package com.api.e_commerce.order;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.AddressService;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.cart.Cart;
import com.api.e_commerce.cart.CartService;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.orderItem.OrderItem;
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
    public Order processCheckout(UUID userId, UUID cartId, UUID shipId, UUID billId) {

        Cart cartEntity = cartService.findByIdAndUserId(cartId, userId);

        var userAddresses = addressService.listAddressesByUserId(userId);

        Address shipAddr = findValidatedAddress(userAddresses, shipId, AddressType.SHIPPING);
        Address billAddr = findValidatedAddress(userAddresses, billId, AddressType.BILLING);

        return createOrderFromCart(cartEntity, shipAddr, billAddr, userId);
    }

    @Transactional
    public Order createOrderFromCart(Cart cart, Address shipAddr, Address billAddr, UUID userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(cart.getTotalAmount());
        order.setShippingAddress(new OrderAddress(shipAddr));
        order.setBillingAddress(new OrderAddress(billAddr));

        orderRepository.save(order);

        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cartItem);
            orderItem.setOrder(order.getId());
            return orderItem;
        }).toList();
        order.setItems(orderItems);

        cartService.clearCartFromCreateOrder(cart);

        return orderRepository.save(order);
    }

    public Address findValidatedAddress(List<Address> userAddresses, UUID addressId, AddressType addressType) {
        return   userAddresses.stream()
                .filter(a -> a.getId().equals(addressId) && a.getAddressType() != null && a.getAddressType().contains(addressType))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Address not found"));
    }

    @Transactional
    public void confirmPayment(UUID orderId, String eventId, String rawPayload) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (order.getStatus() == OrderStatus.PAID) return;

        for (OrderItem item : order.getItems()) {
            productService.decreaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);
        paymentService.updatePaymentStatus(order, PaymentTransactionStatus.SUCCESS, eventId, rawPayload);
    }

}
