package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderRepository;
import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.PaymentTransaction;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionType;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;
import com.api.e_commerce.payment.gateways.PaymentGateway;
import com.api.e_commerce.payment.gateways.stripe.StripePaymentGatewayImpl;
import com.api.e_commerce.payment.repository.PaymentRepository;
import com.api.e_commerce.payment.repository.PaymentTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripePaymentGatewayImpl paymentGateway;
    private final PaymentTransactionRepository transactionRepository;

    @Transactional
    public CreateCheckoutSessionResponse createCheckoutSession(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found"));

        Payment payment = getOrCreatePayment(order);

        try{
            //CreateCheckoutSessionResponse
            System.out.println(order.getItems().size());
            var response = paymentGateway.createCheckoutSession(order);

            payment.attachCheckoutSessionId(response.sessionId());

            payment.setPaymentStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);

            registerTransactionDetails(payment, response.sessionId(), PaymentTransactionType.CHECKOUT_SESSION_CREATED, PaymentTransactionStatus.SUCCESS, null);

            return response;
        }catch (RuntimeException e){
            registerTransactionDetails(payment, null, PaymentTransactionType.PAYMENT_FAILED, PaymentTransactionStatus.FAILURE, e.getMessage());

            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);

            throw new PaymentGatewayException("Failed to iniate payment: " + e.getMessage());
        }
    }

    private Payment getOrCreatePayment(Order order) {
        return paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setOrderId(order.getId());
                    newPayment.setAmount(order.getTotalAmount());
                    newPayment.setCurrency(order.getCurrency());
                    newPayment.setPaymentStatus(PaymentStatus.PENDING);
                    newPayment.setProvider(PaymentProvider.STRIPE);
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

        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        paymentRepository.save(payment);

        var transaction = new PaymentTransaction();
        transaction.setPaymentId(payment.getId());
        transaction.setType(PaymentTransactionType.PAYMENT_CONFIRMED);
        transaction.setStatus(status);           // "SUCCESS"
        transaction.setProviderEventId(eventId); // evt_... do Stripe para idempotência
        transaction.setRawPayload(rawPayload);   // O JSONB que discutimos (muito importante!)
        transaction.setCreatedAt(Instant.now());

        transactionRepository.save(transaction);

    }
}
