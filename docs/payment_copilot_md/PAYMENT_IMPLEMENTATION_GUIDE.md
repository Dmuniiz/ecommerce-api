# 🚀 Payment Service Implementation Guide

## Quick Start

### 1. Database Migrations (Priority)
Create `src/main/resources/db/migration/V15__payment_retries.sql`:

```sql
-- Payment Retry Table
CREATE TABLE payment_retries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL UNIQUE REFERENCES payments(id) ON DELETE CASCADE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP,
    last_error_message TEXT,
    is_retryable BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_retry_id ON payment_retries(payment_id);
CREATE INDEX idx_next_retry_at ON payment_retries(next_retry_at, is_retryable);
```

### 2. Add Configuration to application.properties

```properties
# ============================================================
# PAYMENT SERVICE CONFIGURATION
# ============================================================

# Timeouts (milliseconds)
payment.checkout-session-timeout-ms=30000
payment.refund-timeout-ms=20000
payment.webhook-processing-timeout-ms=10000

# Retry Strategy
payment.max-retry-attempts=3
payment.initial-backoff-ms=1000
payment.max-backoff-ms=30000
payment.backoff-multiplier=2.0

# Webhook Configuration
payment.enable-webhook-retry=true
payment.max-webhook-retries=5
payment.webhook-retry-interval-ms=5000

# Concurrency Prevention
payment.enable-concurrent-payment-prevention=true
payment.concurrent-payment-lock-timeout-secs=300

# Scheduler Configuration
payment.retry.scheduler-interval-ms=300000

# Callback URLs (adjust for your frontend)
payment.success-url=${FRONTEND_URL:http://localhost:3000}/payment/success
payment.cancel-url=${FRONTEND_URL:http://localhost:3000}/payment/cancel

# ============================================================
# STRIPE CONFIGURATION
# ============================================================
stripe.secret-key=${STRIPE_SECRET_KEY}
stripe.webhook.secret-key=${STRIPE_WEBHOOK_SECRET}
```

### 3. Enable Scheduling in Main Application
Update `ECommerceApplication.java`:

```java
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Add this
public class ECommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }
}
```

### 4. Add PaymentRetryRepository Bean (Optional - Auto-discovered)
The repository will be auto-discovered via component scan. If needed, add to a configuration class:

```java
@Configuration
public class PaymentRepositoryConfig {
    // Auto-discovered via @EnableJpaRepositories (default)
    // No explicit bean definition needed
}
```

---

## New Endpoints Summary

### Create Checkout Session
```
POST /api/v1/payments/{orderId}/checkout-session
Authorization: Bearer {token}
Content-Type: application/json

{
  "provider": "STRIPE"
}

Response: 200 OK
{
  "checkoutUrl": "https://checkout.stripe.com/...",
  "sessionId": "cs_live_..."
}
```

### Get Payment Details
```
GET /api/v1/payments/{paymentId}
Authorization: Bearer {token}

Response: 200 OK
{
  "paymentId": "uuid",
  "orderId": "uuid",
  "status": "SUCCEEDED",
  "provider": "STRIPE",
  "amount": 99.99,
  "currency": "BRL",
  "failureReason": null,
  "paidAt": "2024-05-22T10:30:00Z",
  "createdAt": "2024-05-22T10:25:00Z",
  "updatedAt": "2024-05-22T10:30:00Z"
}
```

### Refund Payment
```
POST /api/v1/payments/{paymentId}/refund?amount=50.00&reason=Customer%20Request
Authorization: Bearer {token}

Response: 200 OK
"Refund processed successfully"
```

---

## Testing the Retry Logic

### Scenario 1: Simulate Payment Retry
1. Create order and attempt payment
2. Stop Stripe service (or inject failure)
3. System automatically retries with exponential backoff
4. Check logs for retry attempts
5. On scheduler run (5 min), retry executes

### Scenario 2: Test Concurrent Prevention
```bash
# Terminal 1
curl -X POST http://localhost:8080/api/v1/payments/{orderId}/checkout-session \
  -H "Authorization: Bearer {token}"

# Terminal 2 (within same minute)
curl -X POST http://localhost:8080/api/v1/payments/{orderId}/checkout-session \
  -H "Authorization: Bearer {token}"

# Expected: Second request returns error
# "Payment already in progress for this order"
```

### Scenario 3: Test Refund
1. Complete a payment
2. Call refund endpoint with amount
3. Verify payment status changes to REFUNDED
4. Check transaction logs for refund record

---

## Monitoring & Logging

### Key Log Lines to Watch
```
INFO - Checkout session created for order UUID with provider STRIPE
INFO - Payment confirmed for order UUID via webhook event evt_...
WARN - Concurrent payment attempt for order UUID
INFO - Found N payments ready for retry
INFO - Retry attempt #X for payment UUID successful
WARN - Payment UUID max retries exhausted
INFO - Payment UUID refunded successfully
```

### Metrics to Track
Track these in your monitoring system:
- `/api/v1/payments/**` - Average response time (should be < 2s)
- Payment success rate (target: > 95%)
- Retry success rate (how many succeed on retry)
- Webhook processing time (target: < 1s)
- Refund success rate

---

## Error Handling Examples

### Payment Already in Progress
```json
{
  "status": 400,
  "message": "Payment already in progress for this order",
  "path": "/api/v1/payments/{orderId}/checkout-session"
}
```

### Refund Amount Exceeds Payment
```json
{
  "status": 400,
  "message": "Refund amount cannot exceed paid amount",
  "path": "/api/v1/payments/{paymentId}/refund"
}
```

### Gateway Timeout
```json
{
  "status": 500,
  "message": "Failed to initiate payment: Gateway timeout",
  "path": "/api/v1/payments/{orderId}/checkout-session"
}
```

---

## Environment Variables Required

```bash
# Stripe
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Frontend URL (for redirects)
FRONTEND_URL=https://myapp.com

# Database (existing)
DB_URL=jdbc:postgresql://localhost:5432/ecommerce
DB_USER=postgres
DB_PASSWORD=...
```

---

## Troubleshooting

### Issue: PaymentRetryRepository not found
**Solution**: Ensure database migration has run and application is restarted

### Issue: Retry scheduler not running
**Solution**: Add `@EnableScheduling` to main application class

### Issue: Webhook signature validation fails
**Solution**: Verify `stripe.webhook.secret-key` is correct and not truncated

### Issue: Retry attempts too frequent
**Solution**: Increase `payment.initial-backoff-ms` in properties

---

## Performance Tuning

### Database Indexes
Verify these indexes exist:
```sql
CREATE INDEX idx_payment_retry_id ON payment_retries(payment_id);
CREATE INDEX idx_next_retry_at ON payment_retries(next_retry_at, is_retryable);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payment_transactions_payment_id ON payment_transactions(payment_id);
```

### Connection Pool
Tune for payment service:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### Scheduler Tuning
```properties
# For high-volume scenarios
payment.retry.scheduler-interval-ms=60000
payment.max-retry-attempts=5
payment.backoff-multiplier=1.5
```

---

## Deployment Checklist

- [ ] Database migrations run successfully
- [ ] All configuration properties set
- [ ] `@EnableScheduling` added to main class
- [ ] PaymentConfig bean created
- [ ] PaymentRetryRepository injected in PaymentService
- [ ] Stripe webhook registered and secret configured
- [ ] Frontend URLs configured correctly
- [ ] Logging configured for payment operations
- [ ] Monitoring/alerting set up
- [ ] Load testing completed
- [ ] Staging environment verified
- [ ] Production deployment plan reviewed

---

## Next Steps After Implementation

1. **Monitor first week**: Watch retry success rates and error patterns
2. **Collect metrics**: Establish baseline for payment success rates  
3. **User feedback**: Gather feedback on error messages
4. **Optimization**: Fine-tune retry configuration based on real data
5. **Phase 2**: Implement idempotency keys for additional protection
6. **Phase 3**: Add payment reconciliation job

---

## Support & Questions

**For implementation issues**: Check the inline Javadoc in:
- `PaymentService.java`
- `PaymentConfig.java`
- `PaymentRetryScheduler.java`

**For payment logic**: Review:
- `StripePaymentStrategyImpl.java` - Provider implementation
- `PaymentController.java` - REST endpoints

**For database**: Refer to:
- `PaymentRetry.java` - Entity mapping
- `V15__payment_retries.sql` - Migration script

---

**Ready to Deploy?** Follow the checklist above and monitor logs in the first 24 hours! 🚀

