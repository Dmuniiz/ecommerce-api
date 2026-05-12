package com.api.e_commerce.payment.domain;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", unique = true, nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "provider_checkout_session_id", unique = true)
    private String providerCheckoutSessionId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(nullable = false)
    private String currency;

    // latest error returned by gateway
    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private Instant paidAt;

    // customer id (optional)
    @Column(name = "provider_customer_id")
    private String providerCustomerId;

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public void attachCheckoutSessionId(String sessionId) {
        this.providerCheckoutSessionId = sessionId;
        this.updatedAt =  Instant.now();
    }

    @PrePersist
    public void prePersist() {
       this.createdAt = Instant.now();
       this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

}
