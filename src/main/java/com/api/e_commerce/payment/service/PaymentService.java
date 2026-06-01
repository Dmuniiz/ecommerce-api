package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderRepository;
import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.PaymentRetry;
import com.api.e_commerce.payment.domain.PaymentTransaction;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionType;
import com.api.e_commerce.payment.dto.PaymentDetailsResponse;
import com.api.e_commerce.payment.dto.PaymentGatewayResponse;
import com.api.e_commerce.payment.gateways.PaymentStrategy;
import com.api.e_commerce.payment.infrastructure.PaymentConfig;
import com.api.e_commerce.payment.repository.PaymentRepository;
import com.api.e_commerce.payment.repository.PaymentRetryRepository;
import com.api.e_commerce.payment.repository.PaymentTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentFactory paymentFactory;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentRetryRepository retryRepository;
    private final PaymentConfig paymentConfig;

    @Transactional
    public PaymentGatewayResponse createCheckoutSession(UUID orderId, PaymentProvider provider, UUID userId) {
        // Validate order exists and belongs to user
        Order order = orderRepository.findOrderByIdAndUser(orderId, userId)
                .orElseThrow(() -> new ValidationException("Order not found"));

        // Prevent concurrent payment attempts for same order
        if (paymentConfig.isEnableConcurrentPaymentPrevention()) {
            Payment existingPayment = paymentRepository.findByOrderId(orderId).orElse(null);
            if (existingPayment != null && PaymentStatus.PENDING.equals(existingPayment.getPaymentStatus())) {
                log.warn("Concurrent payment attempt for order {}", orderId);
                throw new ValidationException("Payment already in progress for this order");
            }
        }

        PaymentStrategy strategy = paymentFactory.getPaymentStrategy(provider.name());
        Payment payment = getOrCreatePayment(order, strategy.getProvider());

        try {
            // Call gateway with timeout
            var response = strategy.createCheckoutSession(order);

            payment.attachCheckoutSessionId(response.sessionId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            registerTransactionDetails(payment, response.sessionId(),
                    PaymentTransactionType.CHECKOUT_SESSION_CREATED,
                    PaymentTransactionStatus.SUCCESS, null);

            log.info("Checkout session created for order {} with provider {}", orderId, provider);
            return response;

        } catch (RuntimeException e) {
            log.error("Payment gateway error for order {}: {}", orderId, e.getMessage(), e);

            registerTransactionDetails(payment, null,
                    PaymentTransactionType.PAYMENT_FAILED,
                    PaymentTransactionStatus.FAILURE, e.getMessage());

            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);

            // Create retry record for failed payment
            createRetryRecord(payment, e.getMessage());

            throw new PaymentGatewayException("Failed to initiate payment: " + e.getMessage());
        }
    }

    private Payment getOrCreatePayment(Order order,  String provider) {
        return paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setOrderId(order.getId());
                    newPayment.setAmount(order.getTotalAmount());
                    newPayment.setCurrency(order.getCurrency());
                    newPayment.setPaymentStatus(PaymentStatus.PENDING);
                    newPayment.setProvider(PaymentProvider.valueOf(provider));
                    return paymentRepository.save(newPayment);
                });
    }

    private void registerTransactionDetails(Payment payment, String providerId, PaymentTransactionType type, PaymentTransactionStatus status, String errorMessage){
        var transaction = new PaymentTransaction();
        transaction.setPaymentId(payment.getId());
        transaction.setType(type);
        transaction.setProviderTransactionId(providerId);
        transaction.setErrorMessage(errorMessage);
        transaction.setStatus(status);

        transactionRepository.save(transaction);
    }

    public void updatePaymentStatus(Order order, PaymentTransactionStatus status, String eventId, String rawPayload){
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new EntityNotFoundException("Payment record not found for order: " + order.getId()));

        // Validate status transition
        validateStatusTransition(payment.getPaymentStatus(), PaymentStatus.SUCCEEDED);

        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        paymentRepository.save(payment);

        // Register confirmation transaction
        registerTransactionDetails(payment, eventId,
                PaymentTransactionType.PAYMENT_CONFIRMED, status, null);

        // Record webhook event
        var transaction = new PaymentTransaction();
        transaction.setPaymentId(payment.getId());
        transaction.setType(PaymentTransactionType.WEBHOOK_RECEIVED);
        transaction.setStatus(status);
        transaction.setProviderEventId(eventId);
        transaction.setRawPayload(rawPayload);
        transaction.setCreatedAt(Instant.now());

        transactionRepository.save(transaction);

        // Clear retry record on success
        retryRepository.findByPaymentId(payment.getId()).ifPresent(retry -> {
            retry.setIsRetryable(false);
            retryRepository.save(retry);
        });

        log.info("Payment confirmed for order {} via webhook event {}", order.getId(), eventId);
    }

    /**
     * Retry failed payment - used by retry scheduler
     */
    @Transactional
    public void retryFailedPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        PaymentRetry retry = retryRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("No retry record found"));

        if (!retry.isReadyForRetry()) {
            log.warn("Payment {} not ready for retry", paymentId);
            return;
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        try {
            PaymentStrategy strategy = paymentFactory.getPaymentStrategy(payment.getProvider().name());
            var response = strategy.createCheckoutSession(order);

            payment.attachCheckoutSessionId(response.getSessionId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            retry.setAttemptCount(retry.getAttemptCount() + 1);
            retry.setNextRetryAt(null);
            retryRepository.save(retry);

            registerTransactionDetails(payment, response.getSessionId(),
                    PaymentTransactionType.CHECKOUT_SESSION_CREATED,
                    PaymentTransactionStatus.SUCCESS, null);

            log.info("Payment {} retry attempt #{} successful", paymentId, retry.getAttemptCount());

        } catch (Exception e) {
            log.error("Payment {} retry attempt #{} failed: {}", paymentId, retry.getAttemptCount() + 1, e.getMessage());

            retry.setAttemptCount(retry.getAttemptCount() + 1);
            retry.setLastErrorMessage(e.getMessage());

            if (retry.isExhausted()) {
                retry.setIsRetryable(false);
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                registerTransactionDetails(payment, null,
                        PaymentTransactionType.PAYMENT_FAILED,
                        PaymentTransactionStatus.FAILURE,
                        "Max retries exhausted: " + e.getMessage());

                log.error("Payment {} max retries exhausted", paymentId);
            } else {
                // Schedule next retry with exponential backoff
                Instant nextRetry = Instant.now().plusMillis(
                    paymentConfig.calculateBackoffDelay(retry.getAttemptCount())
                );
                retry.setNextRetryAt(nextRetry);
            }

            retryRepository.save(retry);
        }
    }

    /**
     * Refund a paid payment
     */
    @Transactional
    public void refundPayment(UUID paymentId, java.math.BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        if (!PaymentStatus.SUCCEEDED.equals(payment.getPaymentStatus())) {
            throw new ValidationException("Only succeeded payments can be refunded. Current status: " + payment.getPaymentStatus());
        }

        if (refundAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be greater than zero");
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new ValidationException("Refund amount cannot exceed paid amount");
        }

        try {
            PaymentStrategy strategy = paymentFactory.getPaymentStrategy(payment.getProvider().name());

            // Call gateway refund if available - use default implementation
            strategy.processRefund(payment.getProviderCheckoutSessionId(), refundAmount);

            // Update payment status
            if (refundAmount.compareTo(payment.getAmount()) == 0) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
            } else {
                // Partial refund - could add PARTIALLY_REFUNDED status in future
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
            }

            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            // Register refund transaction
            registerTransactionDetails(payment, UUID.randomUUID().toString(),
                    PaymentTransactionType.REFUND_COMPLETED,
                    PaymentTransactionStatus.SUCCESS, reason);

            log.info("Payment {} refunded successfully. Amount: {}, Reason: {}", paymentId, refundAmount, reason);

        } catch (Exception e) {
            log.error("Refund failed for payment {}: {}", paymentId, e.getMessage(), e);

            registerTransactionDetails(payment, null,
                    PaymentTransactionType.REFUND_CREATED,
                    PaymentTransactionStatus.FAILURE, e.getMessage());

            throw new PaymentGatewayException("Refund failed: " + e.getMessage());
        }
    }

    /**
     * Get payment details
     */
    public PaymentDetailsResponse getPaymentDetails(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        return new PaymentDetailsResponse(
                payment.getId().toString(),
                payment.getOrderId().toString(),
                payment.getPaymentStatus().name(),
                payment.getProvider().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFailureReason(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    /**
     * Validate payment status transition
     */
    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new ValidationException("Payment status cannot be null");
        }

        // Define valid transitions
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == PaymentStatus.SUCCEEDED ||
                          newStatus == PaymentStatus.FAILED ||
                          newStatus == PaymentStatus.CANCELLED;
            case SUCCEEDED -> newStatus == PaymentStatus.REFUNDED;
            case FAILED, CANCELLED, REFUNDED -> false;
        };

        if (!validTransition) {
            throw new ValidationException("Invalid payment status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void createRetryRecord(Payment payment, String errorMessage) {
        PaymentRetry retry = new PaymentRetry();
        retry.setPaymentId(payment.getId());
        retry.setMaxAttempts(paymentConfig.getMaxRetryAttempts());
        retry.setAttemptCount(0);
        retry.setLastErrorMessage(errorMessage);
        retry.setIsRetryable(true);
        retry.setNextRetryAt(Instant.now().plusMillis(paymentConfig.getInitialBackoffMs()));

        retryRepository.save(retry);
    }
}
