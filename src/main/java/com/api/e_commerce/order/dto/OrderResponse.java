package com.api.e_commerce.order.dto;

import com.api.e_commerce.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        OrderAddressResponse shippingAddress,
        Boolean sameAsShipping,
        OrderAddressResponse billingAddress,
        List<OrderItemResponse> items
) {

}