package com.api.e_commerce.cart;

import com.api.e_commerce.cart.cartItem.CartItem;
import com.api.e_commerce.product.Product;
import com.api.e_commerce.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    public Cart(User user) {
        this.user = user;
    }

    public void calculateTotalAmount() {
        totalAmount = cartItems.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(Product product, Integer quantity) {
        cartItems.stream()
                .filter(c -> c.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        cartItem -> cartItem.addQuantity(quantity),
                        () -> cartItems.add(new CartItem(this, product, quantity))
                );
        refreshTotals();
    }

    public boolean findExistingItemByProduct(Product product) {
        return cartItems.stream()
                .anyMatch(c -> c.getProduct().getId().equals(product.getId()));
    }

    public Integer getQuantityByProductId(UUID productId) {
        return cartItems.stream()
                .filter(c -> c.getProduct().getId().equals(productId))
                .map(CartItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    public void changeItemQuantity(UUID productId, Integer quantity) {
        cartItems.stream()
                .filter(c -> c.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new com.api.e_commerce.config.exception.ValidationException("Item not found in cart"))
                .changeQuantity(quantity);

        refreshTotals();
    }

    public boolean removeItemByProductId(UUID productId) {
        boolean removed = cartItems.removeIf(c -> c.getProduct().getId().equals(productId));
        if (removed) {
            refreshTotals();
        }
        return removed;
    }

    public void clearItems() {
        cartItems.clear();
        refreshTotals();
    }

    private void refreshTotals() {
        calculateTotalAmount();
        updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
