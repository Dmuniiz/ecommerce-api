package com.api.e_commerce.order;

import com.api.e_commerce.order.dto.CancelOrderRequest;
import com.api.e_commerce.order.dto.CreateOrderRequest;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.dto.OrderTrackingResponse;
import com.api.e_commerce.order.mapper.OrderMapper;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder
    ) {
        //@param idempotencyKey Optional header for idempotent requests
        var createdOrder = orderService.createOrder(
                user.getId(),
                request.cartId(),
                request.shippingAddressId(),
                request.billingAddressId(),
                idempotencyKey
        );

        OrderResponse response = orderMapper.toOrderResponse(createdOrder);

        URI locationUri = builder.path("/api/v1/orders/{id}").buildAndExpand(createdOrder.getId()).toUri();

        //@return 201 Created with order details
        return ResponseEntity.created(locationUri).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var ordersPage = orderService.listUserOrders(user.getId(), page, size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(ordersPage.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(ordersPage.getTotalPages()))
                .body(ordersPage);
    }

    /**
     * @return 200 OK with order details
     * @throws OrderNotFoundException if order not found
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User user
    ) {
        Order order = orderService.getOrderDetails(orderId, user.getId());
        OrderResponse response = orderMapper.toOrderResponse(order);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/status")
    public ResponseEntity<Page<OrderResponse>> searchByStatus(
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var ordersPage = orderService.listOrdersByStatus(user.getId(), status, page, size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(ordersPage.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(ordersPage.getTotalPages()))
                .body(ordersPage);
    }

    /**
     * Get order tracking information with timeline
     *
     * @param orderId Order UUID
     * @param user Authenticated user
     * @return 200 OK with tracking information
     */
    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderTrackingResponse> trackOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User user
    ) {
        OrderTrackingResponse tracking = orderService.getOrderTracking(orderId, user.getId());

        return ResponseEntity.ok(tracking);
    }


    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid CancelOrderRequest request,
            @AuthenticationPrincipal User user
    ) {
        Order cancelledOrder = orderService.cancelOrder(orderId, user.getId(), request.reason());
        OrderResponse response = orderMapper.toOrderResponse(cancelledOrder);

        return ResponseEntity.ok(response);
    }
}
