package com.api.e_commerce.order;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.AddressService;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.cart.Cart;
import com.api.e_commerce.cart.CartService;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.dto.CheckoutOrderRequest;
import com.api.e_commerce.order.orderItem.OrderItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AddressService addressService;


    @Transactional
    public Order processCheckout(UUID userId, UUID cartId, UUID shipId, UUID billId) {

        Cart cartEntity = cartService.findByIdAndUserId(cartId, userId);

        var userAddresses = addressService.listAddressesByUserId(userId);

        Address shipAddr = findValidatedAddress(userAddresses, shipId, AddressType.SHIPPING);
        Address billAddr = findValidatedAddress(userAddresses, billId, AddressType.BILLING);

        var savedOrder =  createOrderFromCart(cartEntity, shipAddr, billAddr, userId);

        cartService.clearCartFromCreateOrder(cartEntity);

        return savedOrder;
    }

    @Transactional
    public Order createOrderFromCart(Cart cart, Address shipAddr, Address billAddr, UUID userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(cart.getTotalAmount());
        order.setShippingAddress(new OrderAddress(shipAddr));
        order.setBillingAddress(new OrderAddress(billAddr));

        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cartItem);
            orderItem.setOrder(order);
            return orderItem;
        }).toList();

        order.setItems(orderItems);

        return orderRepository.save(order);
    }

    public Address findValidatedAddress(List<Address> userAddresses, UUID addressId, AddressType addressType) {
        return   userAddresses.stream()
                .filter(a -> a.getId().equals(addressId) && a.getAddressType() != null && a.getAddressType().contains(addressType))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Address not found"));
    }


}
