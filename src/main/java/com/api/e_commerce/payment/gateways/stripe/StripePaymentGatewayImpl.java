package com.api.e_commerce.payment.gateways.stripe;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.orderItem.OrderItem;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;
import com.api.e_commerce.payment.gateways.PaymentGateway;
import com.api.e_commerce.payment.infrastructure.StripeProperties;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripePaymentGatewayImpl implements PaymentGateway {

    private final StripeProperties properties;

    public StripePaymentGatewayImpl(StripeProperties properties) {
        this.properties = properties;
    }

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(Order order) {

        try {
            SessionCreateParams.Builder params =  SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/payment/success?orderId="+order.getId())
                    .setCancelUrl("http://localhost:8080/payment/cancel");

            System.out.println(order.getItems().size());

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
                                                .setUnitAmount(convertToStripeAmount(item.getPriceAtPurchase()))
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

            return new  CreateCheckoutSessionResponse(
                    session.getUrl(),
                    session.getId()
            );

        } catch (StripeException e) {
            throw new PaymentGatewayException(e.getMessage());
        }
    }

    private Long convertToStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

}
