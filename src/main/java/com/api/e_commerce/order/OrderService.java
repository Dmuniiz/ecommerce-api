package com.api.e_commerce.order;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.AddressService;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.cart.Cart;
import com.api.e_commerce.cart.CartService;
import com.api.e_commerce.cart.cartItem.CartItem;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.orderItem.OrderItem;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.service.PaymentService;
import com.api.e_commerce.product.ProductService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final AddressService addressService;
    private final PaymentService paymentService;


    @Transactional(readOnly = true)
    public List<Order> listUserOrders(UUID userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Order createOrder(UUID userId, UUID cartId, UUID shipId, UUID billId) {
        Cart cart = cartService.findByIdAndUserId(cartId, userId);

        List<Address> addresses = addressService.listAddressesByUserId(userId);

        Address shipping = validateAddress(addresses, shipId, AddressType.SHIPPING);
        Address billing = validateAddress(addresses, billId, AddressType.BILLING);

        Order order = buildOrder(userId, cart, shipping, billing);

        for(OrderItem orderItem : order.getItems()) {
            System.out.println(orderItem.getProduct().getName());
        }

        cartService.clearCartFromCreateOrder(cart);

        return orderRepository.save(order);
    }

    private Order buildOrder(UUID userId, Cart cart, Address shipping, Address billing) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(cart.getTotalAmount());
        order.setShippingAddress(new OrderAddress(shipping));
        order.setBillingAddress(new OrderAddress(billing));

        List<OrderItem> items = cart.getCartItems().stream()
                .map(cartItem -> buildOrderItem(order, cartItem))
                .toList();

        order.setItems(items);

        return order;
    }

    private OrderItem buildOrderItem(Order order, CartItem cartItem) {
        OrderItem item = new OrderItem();
        item.setItem(cartItem);
        item.setOrder(order.getId());

        System.out.println(order.getId());
        System.out.println(item.getOrderId());

        return item;
    }

    private Address validateAddress(
            List<Address> addresses,
            UUID addressId,
            AddressType type
    ) {
        return addresses.stream()
                .filter(address ->
                        address.getId().equals(addressId)
                                && address.getAddressType() != null
                                && address.getAddressType().contains(type)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ValidationException(
                                "Address %s not found or invalid for %s"
                                        .formatted(addressId, type)
                        )
                );
    }

    @Transactional
    public void confirmPayment(UUID orderId, String eventId, String rawPayload) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID) return;

        for (OrderItem item : order.getItems()) {
            productService.decreaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);
        paymentService.updatePaymentStatus(order, PaymentTransactionStatus.SUCCESS, eventId, rawPayload);
    }

}
