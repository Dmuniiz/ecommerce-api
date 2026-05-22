package com.api.e_commerce.payment.gateways.stripe;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.orderItem.OrderItem;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.dto.PaymentGatewayResponse;
import com.api.e_commerce.payment.gateways.PaymentStrategy;
import com.api.e_commerce.payment.infrastructure.StripeProperties;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("STRIPE")
public class StripePaymentStrategyImpl implements PaymentStrategy {

    private final StripeProperties properties;

    public StripePaymentStrategyImpl(StripeProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentGatewayResponse createCheckoutSession(Order order) {

        try {
            SessionCreateParams.Builder params =  SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/payment/success?orderId="+order.getId())
                    .setCancelUrl("http://localhost:8080/payment/cancel");

            for(OrderItem item : order.getItems()) {
                params.addLineItem(
                        SessionCreateParams
                                .LineItem
                                .builder()
                                .setQuantity((long) item.getQuantity())
                                .setPriceData(
                                        SessionCreateParams
                                                .LineItem
                                                .PriceData
                                                .builder()
                                                .setCurrency(order.getCurrency().toLowerCase())
                                                .setUnitAmount(ConvertToAmount(item.getPriceAtPurchase()))
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData
                                                                .ProductData
                                                                .builder()
                                                                .setName(item.getProduct().getName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );
            }

            params.putMetadata("orderId", String.valueOf(order.getId()));

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(properties.getSECRET_KEY())
                    .build();

            Session session = Session.create(params.build(), requestOptions);

            return new PaymentGatewayResponse(
                    session.getUrl(),
                    session.getId()
            );

        } catch (StripeException e) {
            throw new PaymentGatewayException(e.getMessage());
        }
    }

    @Override
    public String getProvider() {
        return PaymentProvider.STRIPE.name();
    }

    @Override
    public Long ConvertToAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

}
