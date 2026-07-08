package com.api.e_commerce.payment;

import com.api.e_commerce.order.OrderService;
import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import com.api.e_commerce.payment.dto.CreateCheckoutRequest;
import com.api.e_commerce.payment.dto.PaymentDetailsResponse;
import com.api.e_commerce.payment.dto.PaymentGatewayResponse;
import com.api.e_commerce.payment.infrastructure.StripeProperties;
import com.api.e_commerce.payment.service.PaymentService;
import com.api.e_commerce.user.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment API endpoints for e-commerce application
 * Handles checkout sessions, webhooks, refunds, and payment status
 */
@Slf4j
@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final StripeProperties stripeProperties;

    /**
     * Create checkout session for order payment
     */
    @PostMapping("/{orderId}/checkout-session")
    public ResponseEntity<PaymentGatewayResponse> createCheckoutSession(
            @PathVariable("orderId") UUID orderId, 
            @RequestBody @Valid CreateCheckoutRequest request, 
            @AuthenticationPrincipal User user) {

        log.info("Creating checkout session for order: {} with provider: {}", orderId, request.provider());
        var response = paymentService.createCheckoutSession(orderId, request.provider(), user.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Get payment details
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsResponse> getPaymentDetails(
            @PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal User user) {

        log.info("Fetching payment details for payment: {}", paymentId);
        var paymentDetails = paymentService.getPaymentDetails(paymentId);

        return ResponseEntity.ok(paymentDetails);
    }

    /**
     * Refund a payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<String> refundPayment(
            @PathVariable("paymentId") UUID paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User user) {

        log.info("Refund requested for payment: {} with amount: {}", paymentId, amount);
        
        try {
            paymentService.refundPayment(paymentId, amount, reason);
            return ResponseEntity.ok("Refund processed successfully");
        } catch (Exception e) {
            log.error("Refund failed for payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Stripe webhook handler
     * Processes payment.charge.succeeded, charge.refunded, and other critical events
     */
    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String rawPayload, 
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(
                    rawPayload, 
                    sigHeader, 
                    stripeProperties.getWEBHOOK_SECRET()
            );
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().build();
        }

        log.info("Received Stripe webhook event: {}", event.getType());

        // Handle different event types
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event, rawPayload);
                break;
            case "charge.refunded":
                handleChargeRefunded(event, rawPayload);
                break;
            case "charge.dispute.created":
                handleChargeDisputeCreated(event, rawPayload);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailed(event, rawPayload);
                break;
            default:
                log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        return ResponseEntity.ok("");
    }

    private void handleCheckoutSessionCompleted(Event event, String rawPayload) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().get();
            String orderId = session.getMetadata().get("orderId");

            if (orderId != null) {
                orderService.confirmPayment(UUID.fromString(orderId), event.getId(), rawPayload);
                log.info("Payment confirmed for order: {} via event: {}", orderId, event.getId());
            }
        } catch (Exception e) {
            log.error("Error processing checkout.session.completed event: {}", e.getMessage(), e);
        }
    }

    private void handleChargeRefunded(Event event, String rawPayload) {
        log.info("Charge refunded event received: {}", event.getId());
        // TODO: Update payment status to REFUNDED
    }

    private void handleChargeDisputeCreated(Event event, String rawPayload) {
        log.warn("Charge dispute created event received: {}. Manual review required.", event.getId());
        // TODO: Alert admins and implement dispute handling
    }

    private void handlePaymentFailed(Event event, String rawPayload) {
        log.warn("Payment failed event received: {}", event.getId());
        // TODO: Implement payment failure handling and notifications
    }
}
