package com.api.e_commerce.order.orderItem;

import com.api.e_commerce.cart.cartItem.CartItem;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; //id + name response

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtPurchase;

    @Column(nullable = false)
    private int quantity;

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setItem(CartItem cartItem) {
       this.product = cartItem.getProduct();
       this.quantity = cartItem.getQuantity();
       this.priceAtPurchase = cartItem.getUnitPrice();
    }
}
