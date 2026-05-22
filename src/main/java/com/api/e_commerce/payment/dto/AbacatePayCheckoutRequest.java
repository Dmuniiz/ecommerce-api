package com.api.e_commerce.payment.dto;

import java.util.List;

public record AbacatePayCheckoutRequest(// Ex: "ONE_TIME"
        List<AbacatePayProductItem> items,
        List<String> methods,
        String externalId,
        String returnUrl,
        String completionUrl
) {
}
