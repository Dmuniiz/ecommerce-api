package com.api.e_commerce.order.mapper;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderAddress;
import com.api.e_commerce.order.dto.OrderAddressResponse;
import com.api.e_commerce.order.dto.OrderItemResponse;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.orderItem.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(final Order order) {
        OrderAddressResponse shippingDto = mapToAddressDto(order.getShippingAddress());
        OrderAddressResponse billingDto = mapToAddressDto(order.getBillingAddress());

        boolean sameAsShipping = shippingDto.equals(billingDto);

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                shippingDto,
                sameAsShipping,
                sameAsShipping ? null : billingDto,
                order.getItems().stream().map(this::mapToItemResponse).toList()
        );
    }

    private OrderAddressResponse mapToAddressDto(final OrderAddress address) {
        if(address == null) return null;
        return new OrderAddressResponse(
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    private OrderItemResponse mapToItemResponse(final OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(), // Supondo que Product tenha name
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }

}
