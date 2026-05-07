package com.api.e_commerce.cart.dto;

import com.api.e_commerce.cart.Cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        BigDecimal totalAmount,
        List<CartItemResponse> products
) {
        public static CartResponse fromEntity(Cart c, List<CartItemResponse> products){
        return new CartResponse(
                c.getId(),
                c.getTotalAmount(),
                products
        );
    }
}
