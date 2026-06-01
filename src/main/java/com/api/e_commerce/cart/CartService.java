package com.api.e_commerce.cart;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.product.Product;
import com.api.e_commerce.product.ProductService;
import com.api.e_commerce.user.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ICartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;

    public Cart listCartByUser(UUID userId) {
        return cartRepository.findByUserWithItems(userId)
                .orElseThrow(() -> new ValidationException("Cart not found or empty"));
    }

    public Cart findByIdAndUserId(UUID cartId, UUID userId) {
        return cartRepository.findCartByIdAndUserId(cartId, userId)
                .orElseThrow(() -> new ValidationException("Cart not found"));
    }

    @Transactional
    public Cart addItemToCart(String productId, Integer quantity, UUID userId) {
        Product product = productService.findByStringParamIdConvertToUUID(productId);
        if (product.getStock() < quantity) {
            throw new ValidationException("Product not enough stock: " + product.getStock()); //400
        }

        var user = userService.findUserById(userId);

        var cart = cartRepository.findByUser(user).orElseGet(() -> new Cart(user));

        if(cart.findExistingItemByProduct(product)){
            throw new ValidationException("Product already in cart");
        }

        cart.addItem(product, quantity);

        return cartRepository.save(cart);
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void removeAbandonedItemFromCart() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        cartRepository.deleteByUpdatedAtBefore(threshold);
    }


    @Transactional
    public void deleteItemFromCart(String itemId, UUID userId) {
        UUID productId = productService.findByStringParamIdConvertToUUID(itemId).getId();

        int deletedRows = cartRepository.deleteCartItemByIdFromUser(productId, userId);

        if (deletedRows == 0) {
            throw new ValidationException("Item not found");
        }

        log.info("User delete item from cart");
    }

    @Transactional
    public void clearCartFromCreateOrder(Cart cart) {
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    public void updateItemQuantity(UUID cartId, UUID itemId, int quantity){

        var cart = cartRepository.getReferenceById(cartId);

         var item = cart.getCartItems()
                .stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item not found in cart"));

         if (quantity > item.getProduct().getStock() || quantity <= 0) {
            throw new ValidationException("Product not enough stock: " + item.getProduct().getStock());
        }

        item.addQuantity(quantity);
        cartRepository.save(cart);
    }
}
