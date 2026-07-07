package com.api.e_commerce.cart.dto;

import com.api.e_commerce.cart.cartItem.CartItem;
import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        Long id,
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static CartItemResponse fromEntity(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getUnitPrice(),
                cartItem.getQuantity(),
                cartItem.getSubTotal()
        );
    }
}
