package com.api.e_commerce.payment;

import com.api.e_commerce.order.OrderService;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;
import com.api.e_commerce.payment.infrastructure.StripeProperties;
import com.api.e_commerce.payment.service.PaymentService;
import com.api.e_commerce.user.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final StripeProperties  stripeProperties;

    @PostMapping("/{orderId}")
    public ResponseEntity<CreateCheckoutSessionResponse> createCheckoutSession(@PathVariable("orderId") UUID orderId, @AuthenticationPrincipal User user) {

        var response = paymentService.createCheckoutSession(orderId, user.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String rawPayload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {

            event = Webhook.constructEvent(rawPayload, sigHeader, stripeProperties.getWEBHOOK_SECRET_KEY());
        }catch (SignatureVerificationException e){
            return ResponseEntity.badRequest().build();
        }

        if("checkout.session.completed".equals(event.getType())){
            Session session = (Session) event.getDataObjectDeserializer().getObject().get();
            String orderId = session.getMetadata().get("orderId");

            orderService.confirmPayment(UUID.fromString(orderId), event.getId(), rawPayload);
        }
        return ResponseEntity.ok("");
    }

}
