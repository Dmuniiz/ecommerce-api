package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.payment.gateways.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentFactory {

    private Map<String, PaymentStrategy> paymentStrategies;

    @Autowired
    public PaymentFactory(List<PaymentStrategy> strategyList) {
        paymentStrategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getProvider, Function.identity()));
    }

    public PaymentStrategy getPaymentStrategy(String provider) {
        var strategy = paymentStrategies.get(provider);
        if(strategy == null) {
            throw new PaymentGatewayException("Unknown provider " + provider);
        }
        return strategy;
    }

}
