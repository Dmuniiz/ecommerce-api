package com.api.e_commerce.payment.gateways;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;

public interface PaymentGateway {

    CreateCheckoutSessionResponse createCheckoutSession(Order order);

}
