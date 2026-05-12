package com.api.e_commerce.payment.domain;

import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Setter
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentTransactionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentTransactionStatus status;

    @Column(name = "provider_event_id")
    private String providerEventId;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "error_message")
    private String errorMessage;
}
