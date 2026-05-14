package com.api.e_commerce.payment;

import com.api.e_commerce.order.OrderService;
import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.dto.CreateCheckoutSessionResponse;
import com.api.e_commerce.payment.gateways.PaymentStrategy;
import com.api.e_commerce.payment.infrastructure.StripeProperties;
import com.api.e_commerce.payment.service.PaymentFactory;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final StripeProperties  stripeProperties;

    @PostMapping("/checkouts/create/{orderId}")
    public ResponseEntity<CreateCheckoutSessionResponse> createCheckoutSession(@PathVariable("orderId") UUID orderId, @RequestParam("provider") PaymentProvider provider, @AuthenticationPrincipal User user) {

        var response = paymentService.createCheckoutSession(orderId, provider);

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


   /* @PostMapping("/webhooks/abacatepay")
    public ResponseEntity<String> handleAbacatePayWebhook(@RequestBody String rawPayload, @RequestHeader("Stripe-Signature") String sigHeader) {

    }*/

    private boolean isValidAbacatePaySignature(String payload, String signatureHeader, String secretKey) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);

            byte[] hashBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return MessageDigest.isEqual(
                    hexString.toString().getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

}
