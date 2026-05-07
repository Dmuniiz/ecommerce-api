package com.api.e_commerce.order;

import com.api.e_commerce.address.AddressService;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.order.dto.CheckoutOrderRequest;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.mapper.OrderMapper;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;


    @PostMapping("{cartId}/checkout")
    public ResponseEntity<OrderResponse> checkoutOrder(@PathVariable UUID cartId, @RequestBody @Valid CheckoutOrderRequest request, @AuthenticationPrincipal User user, UriComponentsBuilder builder) {
        //verificar se o carrtId possui o mesmo user do carrinho

        Order createdOrder = orderService.processCheckout(
                user.getId(),
                cartId,
                UUID.fromString(request.shippingAddressId()),
                UUID.fromString(request.billingAddressId())
        );

        OrderResponse response = orderMapper.toOrderResponse(createdOrder);
        URI uri = builder.path("/orders/{id}").buildAndExpand(createdOrder.getId()).toUri(); //GET > USER

        return ResponseEntity.created(uri).body(response);
    }

}
