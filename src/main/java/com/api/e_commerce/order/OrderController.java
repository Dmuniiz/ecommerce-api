package com.api.e_commerce.order;

import com.api.e_commerce.order.dto.CreateOrderRequest;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.mapper.OrderMapper;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;


    @PostMapping("/{cartId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable UUID cartId, @RequestBody @Valid CreateOrderRequest request, @AuthenticationPrincipal User user, UriComponentsBuilder builder) {
        var createdOrder = orderService.createOrder(
                user.getId(),
                cartId,
                request.shippingAddressId(),
                request.billingAddressId()
        );

        OrderResponse response = orderMapper.toOrderResponse(createdOrder);

        URI locationUri  = builder.path("/orders/{id}").buildAndExpand(createdOrder.getId()).toUri(); //GET > USER

        return ResponseEntity.created(locationUri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@AuthenticationPrincipal User user) {
        List<Order> orders = orderService.listUserOrders(user.getId());

        List<OrderResponse> response = orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /*@PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId, @AuthenticationPrincipal User user) {
        Order order = orderService.cancelOrder(orderId, user.getId());

        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }*/

}
