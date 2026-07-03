package com.api.e_commerce.payment.service;

import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.PaymentTransaction;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionType;
import com.api.e_commerce.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Factory for creating and recording payment transactions
 * Centraliza toda a lógica de criação de transações
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTransactionFactory {

    private final PaymentTransactionRepository transactionRepository;

    // factory method -> creator
    private void createTransaction(UUID paymentId, PaymentTransactionType type,
                                   String providerId, PaymentTransactionStatus status, String message) {
        var transaction = PaymentTransaction.builder()
                .paymentId(paymentId)
                .type(type)
                .providerTransactionId(providerId)
                .status(status)
                .errorMessage(message)
                .createdAt(Instant.now())
                .build();

        transactionRepository.save(transaction);
    }

    /**
     * Registra uma sessão de checkout criada com sucesso
     */
    public void recordCheckoutCreated(Payment payment, String sessionId) {
        log.debug("Recording checkout created for payment {}", payment.getId());
        createTransaction(
            payment.getId(),
            PaymentTransactionType.CHECKOUT_SESSION_CREATED,
            sessionId,
            PaymentTransactionStatus.SUCCESS,
            null
        );
    }

    /**
     * Registra falha ao criar sessão de checkout
     */
    public void recordCheckoutFailed(Payment payment, String errorMessage) {
        log.debug("Recording checkout failed for payment {}: {}", payment.getId(), errorMessage);
        createTransaction(
            payment.getId(),
            PaymentTransactionType.PAYMENT_FAILED,
            null,
            PaymentTransactionStatus.FAILURE,
            errorMessage
        );
    }

    /**
     * Registra confirmação de pagamento via webhook
     */
    public void recordPaymentConfirmed(Payment payment, String eventId, String rawPayload) {
        log.debug("Recording payment confirmed for payment {} via event {}", payment.getId(), eventId);
        createTransaction(
            payment.getId(),
            PaymentTransactionType.PAYMENT_CONFIRMED,
            eventId,
            PaymentTransactionStatus.SUCCESS,
            rawPayload
        );
    }

    /**
     * Registra recebimento de webhook
     */
    public void recordWebhookReceived(Payment payment, String eventId, String rawPayload) {
        log.debug("Recording webhook received for payment {} (event: {})", payment.getId(), eventId);
        var transaction = PaymentTransaction.builder()
            .paymentId(payment.getId())
            .type(PaymentTransactionType.WEBHOOK_RECEIVED)
            .providerEventId(eventId)
            .status(PaymentTransactionStatus.SUCCESS)
            .rawPayload(rawPayload)
            .createdAt(Instant.now())
            .build();
        transactionRepository.save(transaction);
    }

    /**
     * Registra reembolso bem-sucedido
     */
    public void recordRefundCompleted(Payment payment, String refundId, String reason) {
        log.debug("Recording refund completed for payment {} (refund: {})", payment.getId(), refundId);
        createTransaction(
            payment.getId(),
            PaymentTransactionType.REFUND_COMPLETED,
            refundId,
            PaymentTransactionStatus.SUCCESS,
            reason
        );
    }

    /**
     * Registra falha de reembolso
     */
    public void recordRefundFailed(Payment payment, String errorMessage) {
        log.debug("Recording refund failed for payment {}: {}", payment.getId(), errorMessage);
        createTransaction(
            payment.getId(),
            PaymentTransactionType.REFUND_CREATED,
            null,
            PaymentTransactionStatus.FAILURE,
            errorMessage
        );
    }

    /**
     * Registra tentativa de retry
     */
    public void recordRetryAttempt(Payment payment, int attemptNumber) {
        log.debug("Recording retry attempt #{} for payment {}", attemptNumber, payment.getId());
        createTransaction(
            payment.getId(),
            PaymentTransactionType.CHECKOUT_SESSION_CREATED,
            null,
            PaymentTransactionStatus.SUCCESS,
            String.format("Retry attempt #%d", attemptNumber)
        );
    }

    /**
     * Registra máximo de tentativas de retry atingido
     */
    public void recordRetryExhausted(Payment payment, String errorMessage) {
        log.debug("Recording retry exhausted for payment {}", payment.getId());
        createTransaction(
            payment.getId(),
            PaymentTransactionType.PAYMENT_FAILED,
            null,
            PaymentTransactionStatus.FAILURE,
            "Max retries exhausted: " + errorMessage
        );
    }
}

