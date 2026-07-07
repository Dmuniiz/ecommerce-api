package com.api.e_commerce.cart;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.product.Product;
import com.api.e_commerce.product.ProductStatus;
import com.api.e_commerce.product.ProductService;
import com.api.e_commerce.user.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ICartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    public Cart getOrCreateCartByUser(UUID userId) {
        return cartRepository.findByUserWithItems(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    @Transactional(readOnly = true)
    public Cart listCartByUser(UUID userId) {
        return cartRepository.findByUserWithItems(userId)
                .orElseThrow(() -> new ValidationException("Cart not found"));
    }

    @Transactional(readOnly = true)
    public Cart findByIdAndUserId(UUID cartId, UUID userId) {
        return cartRepository.findCartByIdAndUserIdWithItems(cartId, userId)
                .orElseThrow(() -> new ValidationException("Cart not found"));
    }

    @Transactional
    public Cart addItemToCart(String productId, Integer quantity, UUID userId) {
        Product product = productService.findByStringParamIdConvertToUUID(productId);
        validateProductAvailable(product);

        var cart = getOrCreateCartForUpdate(userId);
        int finalQuantity = cart.getQuantityByProductId(product.getId()) + quantity;
        validateStock(product, finalQuantity);

        cart.addItem(product, quantity);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(String productId, int quantity, UUID userId){
        Product product = productService.findByStringParamIdConvertToUUID(productId);
        validateProductAvailable(product);
        validateStock(product, quantity);

        var cart = getOrCreateCartForUpdate(userId);
        cart.changeItemQuantity(product.getId(), quantity);

        return cartRepository.save(cart);
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void removeAbandonedItemFromCart() {
        Instant threshold = Instant.now().minus(java.time.Duration.ofDays(7));
        cartRepository.deleteByUpdatedAtBefore(threshold);
    }


    @Transactional
    public Cart deleteItemFromCart(String productId, UUID userId) {
        UUID parsedProductId = productService.findByStringParamIdConvertToUUID(productId).getId();
        var cart = getOrCreateCartForUpdate(userId);

        if (!cart.removeItemByProductId(parsedProductId)) {
            throw new ValidationException("Item not found");
        }

        log.info("User {} deleted product {} from cart", userId, parsedProductId);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        var cart = getOrCreateCartForUpdate(userId);
        cart.clearItems();
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCartFromCreateOrder(Cart cart) {
        cart.clearItems();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCartForUpdate(UUID userId) {
        return cartRepository.findByUserWithItemsForUpdate(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    private Cart createCartForUser(UUID userId) {
        var user = userService.findUserById(userId);
        return cartRepository.save(new Cart(user));
    }

    private void validateProductAvailable(Product product) {
        if (product.getProductStatus() != ProductStatus.AVAILABLE) {
            throw new ValidationException("Product is not available");
        }
    }

    private void validateStock(Product product, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        if (product.getStock() < quantity) {
            throw new ValidationException("Product not enough stock: " + product.getStock());
        }
    }
}
