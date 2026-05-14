package com.api.e_commerce.payment.gateways.stripe;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.dto.AbacatePayCheckoutRequest;
import com.api.e_commerce.payment.dto.AbacatePayProductItem;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;
import com.api.e_commerce.payment.gateways.PaymentStrategy;
import com.api.e_commerce.payment.infrastructure.AbacatePayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component("ABACATEPAY")
@RequiredArgsConstructor
public class AbacatePayStrategyImpl implements PaymentStrategy {

    private final RestClient abacatePayClient;
    private final AbacatePayProperties abacatePayProperties;

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(Order order) {

        List<AbacatePayProductItem> apiItems = order.getItems().stream()
                .map(item -> new AbacatePayProductItem(
                        item.getProduct().getId().toString(),
                        item.getQuantity()
                )).toList();


        var checkoutRequestBody = new AbacatePayCheckoutRequest(
                apiItems,
                List.of("PIX"), // Métodos de pagamento aceitos
                "http://localhost:8080/payment/success?orderId="+order.getId(),
                "http://localhost:8080/payment/cancel"
        );

        return abacatePayClient
                .post()
                .uri("/checkouts/create")
                .header("Authorization", "Bearer " + abacatePayProperties.getSECRET_KEY())
                .contentType(MediaType.APPLICATION_JSON)
                .body(checkoutRequestBody)
                .retrieve()
                .body(CreateCheckoutSessionResponse.class);

    }

    @Override
    public String getProvider() {
        return PaymentProvider.ABACATEPAY.name();
    }

    @Override
    public Long ConvertToAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
}
