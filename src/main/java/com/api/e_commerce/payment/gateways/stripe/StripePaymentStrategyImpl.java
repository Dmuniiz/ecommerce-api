package com.api.e_commerce.payment.gateways.stripe;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.orderItem.OrderItem;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.dto.PaymentGatewayResponse;
import com.api.e_commerce.payment.gateways.PaymentStrategy;
import com.api.e_commerce.payment.infrastructure.StripeProperties;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stripe payment gateway implementation
 * Handles payment checkout sessions and refunds via Stripe API
 */
@Slf4j
@Component("STRIPE")
public class StripePaymentStrategyImpl implements PaymentStrategy {

    private final StripeProperties properties;

    public StripePaymentStrategyImpl(StripeProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentGatewayResponse createCheckoutSession(Order order) {
        try {
            SessionCreateParams.Builder params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(getCallbackUrl("/success?orderId=" + order.getId()))
                    .setCancelUrl(getCallbackUrl("/cancel"));

            // Add line items for each product in order
            for (OrderItem item : order.getItems()) {
                params.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) item.getQuantity())
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(order.getCurrency().toLowerCase())
                                                .setUnitAmount(ConvertToAmount(item.getPriceAtPurchase()))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(item.getProduct().getName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );
            }

            // Add order metadata for webhook processing
            params.putMetadata("orderId", String.valueOf(order.getId()));
            params.putMetadata("userId", order.getUserId().toString());

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(properties.getSECRET_KEY())
                    .build();

            Session session = Session.create(params.build(), requestOptions);

            log.info("Stripe checkout session created: {} for order: {}", session.getId(), order.getId());

            return new PaymentGatewayResponse(
                    session.getUrl(),
                    session.getId()
            );

        } catch (StripeException e) {
            log.error("Stripe checkout session creation failed: {}", e.getUserMessage());
            throw new PaymentGatewayException("Stripe error: " + mapStripeError(e));
        }
    }

    @Override
    public String processRefund(String paymentIntentId, BigDecimal refundAmount) {
        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(properties.getSECRET_KEY())
                    .build();

            RefundCreateParams params = RefundCreateParams.builder()
                    .setCharge(paymentIntentId)
                    .setAmount(ConvertToAmount(refundAmount))
                    .build();

            Refund refund = Refund.create(params, requestOptions);

            log.info("Stripe refund created: {} for charge: {}", refund.getId(), paymentIntentId);
            return refund.getId();

        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getUserMessage());
            throw new PaymentGatewayException("Stripe refund error: " + mapStripeError(e));
        }
    }

    @Override
    public String getProvider() {
        return PaymentProvider.STRIPE.name();
    }

    @Override
    public Long ConvertToAmount(BigDecimal amount) {
        // Stripe uses smallest currency unit (e.g., cents for USD)
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private String getCallbackUrl(String path) {
        String baseUrl = System.getenv("PAYMENT_CALLBACK_BASE_URL");
        if (baseUrl == null) {
            baseUrl = "http://localhost:3000";
        }
        return baseUrl + "/payment" + path;
    }

    private String mapStripeError(StripeException e) {
        return switch (e.getCode()) {
            case "card_declined" -> "Payment was declined. Please use a different card.";
            case "expired_card" -> "Card has expired.";
            case "incorrect_cvc" -> "Incorrect CVC code.";
            case "processing_error" -> "Processing error. Please try again.";
            case "rate_limit" -> "Too many requests. Please try again later.";
            default -> e.getUserMessage() != null ? e.getUserMessage() : "Stripe transaction failed";
        };
    }
}
