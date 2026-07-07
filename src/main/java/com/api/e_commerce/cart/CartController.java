package com.api.e_commerce.cart;

import com.api.e_commerce.cart.dto.AddToCartRequest;
import com.api.e_commerce.cart.dto.CartItemResponse;
import com.api.e_commerce.cart.dto.CartResponse;
import com.api.e_commerce.cart.dto.UpdateCartItemRequest;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCurrentUserCart(@AuthenticationPrincipal User user){

        return ResponseEntity.ok(toResponse(cartService.getOrCreateCartByUser(user.getId())));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> patchItemQuantityInCart(@PathVariable String productId,
                                                                @RequestBody @Valid UpdateCartItemRequest request,
                                                                @AuthenticationPrincipal User user){

        var cart = cartService.updateItemQuantity(productId, request.quantity(), user.getId());
        return ResponseEntity.ok(toResponse(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@RequestBody @Valid AddToCartRequest request, @AuthenticationPrincipal User user) {
        var cart = cartService.addItemToCart(request.productId(), request.quantity(), user.getId());

        return ResponseEntity.ok(toResponse(cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable String productId, @AuthenticationPrincipal User user) {
        var cart = cartService.deleteItemFromCart(productId, user.getId());

        return ResponseEntity.ok(toResponse(cart));
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }

    private CartResponse toResponse(Cart cart) {
        var cartItems = cart.getCartItems()
                .stream()
                .map(CartItemResponse::fromEntity)
                .toList();

        return CartResponse.fromEntity(cart, cartItems);
    }
}
