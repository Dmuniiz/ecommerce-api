package com.api.e_commerce.cart.cartItem;


import com.api.e_commerce.cart.Cart;
import com.api.e_commerce.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    public void addQuantity(Integer qty) {
        this.quantity += qty;
    }

    public BigDecimal getSubTotal() {
        //unitPrice * quantity
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

}
