package com.api.e_commerce.order.dto;

import com.api.e_commerce.order.OrderStatus;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Complete order response with all relevant information
 * including payment details and timestamps for tracking
 */
public record OrderResponse(
        UUID id,
        OrderStatus status,
        UUID paymentId,
        PaymentStatus paymentStatusSnapshot,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        Instant updatedAt,
        Instant paidAt,
        Instant shippedAt,
        OrderAddressResponse shippingAddress,
        Boolean sameAsShipping,
        OrderAddressResponse billingAddress,
        List<OrderItemResponse> items
) {

}