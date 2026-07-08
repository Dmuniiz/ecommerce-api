package com.api.e_commerce.order;

import com.api.e_commerce.order.orderItem.OrderItem;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_payment_id", columnList = "payment_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "payment_id", unique = true)
    private UUID paymentId;

    @Column(name = "payment_status_snapshot")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatusSnapshot;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
            @AttributeOverride(name = "number", column = @Column(name = "shipping_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "shipping_complement")),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "shipping_neighborhood")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private OrderAddress shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
            @AttributeOverride(name = "number", column = @Column(name = "billing_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "billing_complement")),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "billing_neighborhood")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "billing_zip_code")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private OrderAddress billingAddress;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 10)
    private String currency = "BRL";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Version
    private Long version;

    public void cancel() {
        if(this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED || this.status == OrderStatus.REFUNDED) {
            throw new  IllegalStateException("Cannot cancel this order in its current state.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void setItems(List<OrderItem> orderItems) {
        this.items.clear();
        if(orderItems != null) {
            orderItems.forEach(this::addItem);
        }
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public void markAsPaid() {
        if(this.status != OrderStatus.PENDING_PAYMENT && this.status != OrderStatus.CREATED) {
            throw new  IllegalStateException("Cannot mark this order as paid from state: " + this.status);
        }
        this.status = OrderStatus.PAID;
        this.paidAt = Instant.now();
        this.updatedAt = Instant.now();
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

    /**
     * Synchronize payment status into order
     */
    public void syncPaymentStatus(PaymentStatus newPaymentStatus, UUID paymentId) {
        this.paymentId = paymentId;
        this.paymentStatusSnapshot = newPaymentStatus;

        switch (newPaymentStatus) {
            case PENDING -> this.status = OrderStatus.PENDING_PAYMENT;
            case SUCCEEDED -> {
                this.status = OrderStatus.PAID;
                this.paidAt = Instant.now();
            }
            case FAILED -> this.status = OrderStatus.PAYMENT_FAILED;
            case REFUNDED -> this.status = OrderStatus.REFUNDED;
            case CANCELLED -> this.status = OrderStatus.CANCELLED;
            default -> {
                // noop for unknown statuses
            }
        }
        this.updatedAt = Instant.now();
    }
}
