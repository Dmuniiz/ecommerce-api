package com.api.e_commerce.order;

import com.api.e_commerce.order.dto.CheckoutOrderRequest;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.mapper.OrderMapper;
import com.api.e_commerce.payment.service.PaymentService;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;


    @PostMapping("{cartId}/checkout")
    public ResponseEntity<OrderResponse> registerOrder(@PathVariable UUID cartId, @RequestBody @Valid CheckoutOrderRequest request, @AuthenticationPrincipal User user, UriComponentsBuilder builder) {
        Order createdOrder = orderService.processCheckout(
                user.getId(),
                cartId,
                UUID.fromString(request.shippingAddressId()),
                UUID.fromString(request.billingAddressId())
        );

        OrderResponse response = orderMapper.toOrderResponse(createdOrder);

        URI locationUri  = builder.path("/orders/{id}").buildAndExpand(createdOrder.getId()).toUri(); //GET > USER

        return ResponseEntity.created(locationUri).body(response);
    }

}
