package com.api.e_commerce.payment.gateways;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.payment.dto.PaymentGatewayResponse;

import java.math.BigDecimal;

public interface PaymentStrategy {

    PaymentGatewayResponse createCheckoutSession(Order order);

    String getProvider();
    Long ConvertToAmount(BigDecimal amount);

}
