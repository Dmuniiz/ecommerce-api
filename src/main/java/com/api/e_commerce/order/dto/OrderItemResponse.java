package com.api.e_commerce.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse (
        UUID productId,
        String productName, // Adicione o nome no OrderItem para snapshot também!
        Integer quantity,
        BigDecimal priceAtPurchase
){
}
