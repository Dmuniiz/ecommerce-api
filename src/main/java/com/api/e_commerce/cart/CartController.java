package com.api.e_commerce.cart;

import com.api.e_commerce.cart.dto.AddToCartRequest;
import com.api.e_commerce.cart.dto.CartItemResponse;
import com.api.e_commerce.cart.dto.CartResponse;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCurrentUserCart(@AuthenticationPrincipal User user){

        var cart = cartService.listCartByUser(user.getId());

        var cartItems = cart.getCartItems()
                .stream()
                .map(CartItemResponse::fromEntity)
                .toList();

        var response = CartResponse.fromEntity(cart, cartItems);

        return ResponseEntity.ok(response);
    }

    /*@PatchMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantityInCart()*/

    @PostMapping("/items")
    public ResponseEntity<List<CartItemResponse>> addToCart(@RequestBody @Valid AddToCartRequest request, @AuthenticationPrincipal User user) {
        var cart = cartService.addItemToCart(request.productId(), request.quantity(), user.getId());

        List<CartItemResponse> cartItemResponseList = cart.getCartItems()
                .stream()
                .map(CartItemResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(cartItemResponseList);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String itemId, @AuthenticationPrincipal User user) {
        cartService.deleteItemFromCart(itemId, user.getId());

        return ResponseEntity.noContent().build();
    }


}
