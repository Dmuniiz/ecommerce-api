package com.api.e_commerce.cart.dto;

import com.api.e_commerce.cart.cartItem.CartItem;
import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal subtotal
) {
    public static CartItemResponse fromEntity(CartItem  cartItem) {
        return new CartItemResponse(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getSubTotal()
        );
    }
}
