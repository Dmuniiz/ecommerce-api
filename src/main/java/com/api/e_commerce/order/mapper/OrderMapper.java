package com.api.e_commerce.order.mapper;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderAddress;
import com.api.e_commerce.order.dto.OrderAddressResponse;
import com.api.e_commerce.order.dto.OrderItemResponse;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.dto.OrderTrackingResponse;
import com.api.e_commerce.order.orderItem.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {
    
    public OrderResponse toOrderResponse(final Order order) {
        OrderAddressResponse shippingDto = mapToAddressDto(order.getShippingAddress());
        OrderAddressResponse billingDto = mapToAddressDto(order.getBillingAddress());

        boolean sameAsShipping = shippingDto != null && shippingDto.equals(billingDto);

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentId(),
                order.getPaymentStatusSnapshot(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPaidAt(),
                order.getShippedAt(),
                shippingDto,
                sameAsShipping,
                sameAsShipping ? null : billingDto,
                order.getItems().stream().map(this::mapToItemResponse).toList()
        );
    }

    /**
     * Map Order to tracking response with timeline
     */
    public OrderTrackingResponse toTrackingResponse(final Order order) {
        List<OrderTrackingResponse.OrderTimelineEvent> timeline = buildTimeline(order);

        return new OrderTrackingResponse(
                order.getId(),
                order.getStatus(),
                timeline
        );
    }

    /**
     * Build timeline of order events based on timestamps and status
     */
    private List<OrderTrackingResponse.OrderTimelineEvent> buildTimeline(final Order order) {
        List<OrderTrackingResponse.OrderTimelineEvent> events = new ArrayList<>();

        // Order created event
        events.add(new OrderTrackingResponse.OrderTimelineEvent(
                order.getStatus(),
                order.getCreatedAt(),
                "Order created"
        ));

        // Payment events
        if (order.getPaidAt() != null) {
            events.add(new OrderTrackingResponse.OrderTimelineEvent(
                    order.getStatus(),
                    order.getPaidAt(),
                    "Payment confirmed"
            ));
        }

        // Shipped event
        if (order.getShippedAt() != null) {
            events.add(new OrderTrackingResponse.OrderTimelineEvent(
                    order.getStatus(),
                    order.getShippedAt(),
                    "Order shipped"
            ));
        }

        // Last update if different from other events
        if (order.getUpdatedAt() != null && !events.stream()
                .anyMatch(e -> e.timestamp().equals(order.getUpdatedAt()))) {
            events.add(new OrderTrackingResponse.OrderTimelineEvent(
                    order.getStatus(),
                    order.getUpdatedAt(),
                    "Order updated: " + order.getStatus()
            ));
        }

        return events;
    }

    private OrderAddressResponse mapToAddressDto(final OrderAddress address) {
        if (address == null) return null;
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
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }

}


