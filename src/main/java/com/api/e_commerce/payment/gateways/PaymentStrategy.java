package com.api.e_commerce.payment.gateways;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;

import java.math.BigDecimal;

public interface PaymentStrategy {

    CreateCheckoutSessionResponse createCheckoutSession(Order order);

    String getProvider();
    Long ConvertToAmount(BigDecimal amount);

}
