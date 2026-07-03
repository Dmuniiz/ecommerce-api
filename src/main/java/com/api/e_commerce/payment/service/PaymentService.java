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
    private final PaymentRetryRepository retryRepository;

    // ============= SERVICES AUX (REFACTOR) =============
    private final PaymentTransactionFactory transactionFactory;
    private final PaymentCheckoutValidator checkoutValidator;
    private final PaymentStatusValidator statusValidator;
    private final PaymentRetryHelper retryHelper;

    @Transactional
    public PaymentGatewayResponse createCheckoutSession(UUID orderId, PaymentProvider provider, UUID userId) {
        Order order = checkoutValidator.validateAndFetchOrder(orderId, userId);
        checkoutValidator.validateNoConcurrentPayment(orderId);

        var payment = createPayment(order, provider);
        return executeCheckout(payment, order, provider);
    }

    @Transactional
    public void updatePaymentStatus(Order order, PaymentTransactionStatus status, String eventId, String rawPayload) {
        Payment payment = findPaymentByOrderOrThrow(order.getId());

        statusValidator.validateTransition(payment.getPaymentStatus(), PaymentStatus.SUCCEEDED);

        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        transactionFactory.recordPaymentConfirmed(payment, eventId, rawPayload);
        transactionFactory.recordWebhookReceived(payment, eventId, rawPayload);

        retryRepository.findByPaymentId(payment.getId())
            .ifPresent(retryHelper::markAsSuccessful);

        // Sync order status with payment
        try {
            order.syncPaymentStatus(payment.getPaymentStatus(), payment.getId());
            orderRepository.save(order);
        } catch (Exception e) {
            log.warn("Failed to sync payment status into order {}: {}", order.getId(), e.getMessage());
        }

        log.info("Payment confirmed for order {} via webhook event {}", order.getId(), eventId);
    }

    @Transactional
    public void retryFailedPayment(UUID paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        PaymentRetry retry = findRetryOrThrow(paymentId);

        if (!retryHelper.isReadyForRetry(retry)) {
            log.debug("Payment {} not ready for retry", paymentId);
            return;
        }

        Order order = findOrderOrThrow(payment.getOrderId());

        try {
            var response = createGatewayCheckoutSession(payment, order);

            payment.attachCheckoutSessionId(response.sessionId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            retryHelper.incrementAttempt(retry, null);
            transactionFactory.recordRetryAttempt(payment, retry.getAttemptCount());

            log.info("Payment {} retry attempt #{} successful", paymentId, retry.getAttemptCount());

        } catch (Exception e) {
            retryHelper.incrementAttempt(retry, e.getMessage());

            if (retryHelper.isExhausted(retry)) {
                retryHelper.markAsExhausted(retry, e.getMessage());
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                transactionFactory.recordRetryExhausted(payment, e.getMessage());
            } else {
                retryHelper.scheduleNextRetry(retry);
            }

            log.error("Payment {} retry attempt #{} failed: {}", paymentId, retry.getAttemptCount(), e.getMessage());
        }
    }

    @Transactional
    public void refundPayment(UUID paymentId, java.math.BigDecimal refundAmount, String reason) {
        Payment payment = findPaymentOrThrow(paymentId);

        checkoutValidator.validateRefund(payment, refundAmount);

        PaymentStrategy strategy = paymentFactory.getPaymentStrategy(payment.getProvider().name());
        strategy.processRefund(payment.getProviderCheckoutSessionId(), refundAmount);

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        transactionFactory.recordRefundCompleted(payment, UUID.randomUUID().toString(), reason);

        log.info("Payment {} refunded: {} {}", paymentId, refundAmount, reason);
    }

    public PaymentDetailsResponse getPaymentDetails(UUID paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);

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

    // ============= PRIVATE METHODS =============

    /**
     * Executa criação de sessão de checkout no gateway
     * Encapsula lógica comum entre createCheckoutSession e retryFailedPayment
     */
    private PaymentGatewayResponse executeCheckout(Payment payment, Order order, PaymentProvider provider) {
        try {
            var response = createGatewayCheckoutSession(payment, order);

            payment.attachCheckoutSessionId(response.sessionId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            transactionFactory.recordCheckoutCreated(payment, response.sessionId());
            log.info("Checkout created for order {} with {}", order.getId(), provider);

            return response;

        } catch (RuntimeException e) {
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);

            transactionFactory.recordCheckoutFailed(payment, e.getMessage());
            retryHelper.createRetryRecord(payment, e.getMessage());

            log.error("Checkout failed for order {}: {}", order.getId(), e.getMessage(), e);
            throw new PaymentGatewayException("Checkout failed: " + e.getMessage());
        }
    }

    /**
     * Helper para criar sessão de checkout no gateway
     */
    private PaymentGatewayResponse createGatewayCheckoutSession(Payment payment, Order order) {
        PaymentStrategy strategy = paymentFactory.getPaymentStrategy(payment.getProvider().name());
        return strategy.createCheckoutSession(order);
    }

    /**
     * create payment or else get
     */
    private Payment createPayment(Order order, PaymentProvider provider) {
        return paymentRepository.findByOrderId(order.getId())
            .orElseGet(() -> {
                Payment newPayment = new Payment();
                newPayment.setOrderId(order.getId());
                newPayment.setAmount(order.getTotalAmount());
                newPayment.setCurrency(order.getCurrency());
                newPayment.setPaymentStatus(PaymentStatus.PENDING);
                newPayment.setProvider(provider);
                return paymentRepository.save(newPayment);
            });
    }

    // ============= FINDERS  =============

    private Payment findPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));
    }

    private PaymentRetry findRetryOrThrow(UUID paymentId) {
        return retryRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new EntityNotFoundException("Retry record not found: " + paymentId));
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private Payment findPaymentByOrderOrThrow(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));
    }
}
