# 🚀 Payment Service - Quick Reference Card

## New Endpoints (Quick API Reference)

```bash
# 1. Create Payment Checkout
POST /api/v1/payments/{orderId}/checkout-session
Header: Authorization: Bearer {token}
Body: { "provider": "STRIPE" }
Response: { "checkoutUrl": "...", "sessionId": "..." }

# 2. Get Payment Details
GET /api/v1/payments/{paymentId}
Header: Authorization: Bearer {token}
Response: PaymentDetailsResponse

# 3. Process Refund
POST /api/v1/payments/{paymentId}/refund?amount=X&reason=Y
Header: Authorization: Bearer {token}
Response: { "status": "200 OK" }

# 4. Stripe Webhook (Auto-handled)
POST /api/v1/payments/webhook/stripe
Header: Stripe-Signature: {signature}
Body: {raw_json_payload}
```

---

## Key Classes & Methods

| Class | Key Methods | Purpose |
|-------|-----------|---------|
| **PaymentService** | `createCheckoutSession()` | Initiates payment |
| | `retryFailedPayment()` | Retries failed payments |
| | `refundPayment()` | Process refunds |
| | `updatePaymentStatus()` | Update on webhook |
| **PaymentRetryScheduler** | `retryFailedPayments()` | Scheduled retry runner |
| **PaymentConfig** | All getters | Access configuration |
| **StripePaymentStrategyImpl** | `createCheckoutSession()` | Create Stripe session |
| | `processRefund()` | Stripe refund |

---

## Configuration Properties

```properties
# MUST CONFIGURE
payment.success-url=https://yourapp.com/payment/success
payment.cancel-url=https://yourapp.com/payment/cancel
stripe.secret-key=sk_live_...
stripe.webhook.secret-key=whsec_...

# OPTIONAL (Defaults provided)
payment.max-retry-attempts=3
payment.initial-backoff-ms=1000
payment.max-backoff-ms=30000
payment.backoff-multiplier=2.0
payment.enable-webhook-retry=true
payment.enable-concurrent-payment-prevention=true
```

---

## Status Codes & Meanings

```
Payment Status     │ Meaning
─────────────────┼────────────────────────────────
PENDING          │ Awaiting customer payment confirmation
SUCCEEDED        │ Payment completed successfully
FAILED           │ Payment failed - needs retry or refund attempt
CANCELLED        │ Customer cancelled checkout
REFUNDED         │ Payment has been refunded to customer
```

---

## Error Messages & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "Payment already in progress" | Concurrent checkout attempt | Wait for current payment or refresh |
| "Payment was declined" | Card declined by issuer | Use different card |
| "Card has expired" | Expired credit card | Update card expiration |
| "Processing error" | Temporary gateway issue | System will auto-retry |
| "Max retries exhausted" | Permanent payment failure | Check account status |
| "Refund amount exceeds..." | Invalid refund amount | Check order amount |

---

## Common Scenarios

### Scenario: Payment Fails, Auto-Retries, Then Succeeds
```
User clicks pay → Stripe timeout
(System logs: PAYMENT_FAILED transaction)
↓ 5 minutes later (scheduler runs)
PaymentRetryScheduler finds failed payment
Calls retryFailedPayment()
Retry succeeds! Payment status → SUCCEEDED
```

### Scenario: User Double-Clicks Checkout
```
Click 1: Creates payment, status = PENDING
Click 2: Validation check→ "Payment already in progress"
→ Prevents duplicate charge ✓
```

### Scenario: Customer Wants Refund
```
Admin calls: POST /refund?amount=100
Validates: Payment SUCCEEDED ✓
Calls Stripe refund API
Payment status → REFUNDED
Customer refund processes in 2-5 business days
```

---

## Logging Keywords (for monitoring)

Search logs for these patterns:

```
"Checkout session created" → Payment initiated
"Payment confirmed" → Payment succeeded
"Retry attempt #" → Payment being retried
"max retries exhausted" → Payment permanently failed
"refunded successfully" → Refund processed
"Concurrent payment attempt" → Duplicate checkout prevented
"Stripe checkout session creation failed" → Gateway error
```

---

## Files to Review

### For Implementation
- `PAYMENT_IMPLEMENTATION_GUIDE.md` ← START HERE
- `PaymentConfig.java` - All configuration options
- `PaymentRetryScheduler.java` - How retries work

### For Architecture
- `PAYMENT_ARCHITECTURE.md` - Visual diagrams & flows
- `PaymentService.java` - Core business logic
- `StripePaymentStrategyImpl.java` - Provider implementation

### For Improvements
- `PAYMENT_IMPROVEMENTS.md` - Detailed improvements
- `PAYMENT_SUMMARY.md` - Business impact
- `V15__payment_retries.sql` - Database migration

---

## Deployment Checklist

```
PRE-DEPLOYMENT
☐ Database migration V15 created
☐ Spring Boot @EnableScheduling added
☐ PropertySource configuration verified
☐ Stripe webhook registered
☐ Frontend URLs configured
☐ Load testing completed

DEPLOYMENT
☐ Run migrations (mvn flyway:migrate)
☐ Deploy application
☐ Monitor logs first 2 hours
☐ Test checkout flow
☐ Test refund flow
☐ Verify webhook processing

POST-DEPLOYMENT
☐ Monitor success rates
☐ Check scheduler logs
☐ Verify retry execution
☐ Collect performance data
☐ Plan optimization
```

---

## Performance Targets

```
Operation              │ Target Performance
──────────────────────┼───────────────────
Payment success rate  │ > 95%
Webhook latency       │ < 1 second
Refund processing     │ < 2 seconds
Retry success rate    │ > 60%
API response time     │ < 2 seconds
```

---

## Useful Commands

```bash
# Check compilation
mvn clean compile

# Run tests
mvn test

# Build artifact
mvn package

# View retry table
SELECT * FROM payment_retries WHERE is_retryable = true;

# Find failed payments
SELECT * FROM payment_retries WHERE attempt_count >= max_attempts;

# View recent transactions
SELECT * FROM payment_transactions 
ORDER BY created_at DESC LIMIT 20;
```

---

## Troubleshooting

| Issue | Check |
|-------|-------|
| PaymentRetryRepository not found | Verify migration V15 ran successfully |
| Scheduler not running | Ensure @EnableScheduling on main class |
| Webhook signature errors | Verify `stripe.webhook.secret-key` value |
| Refunds failing | Check Stripe secret key permissions |
| Concurrent payment allowed | Verify PaymentConfig bean created |
| Status transition errors | Check current payment status |

---

## Support Resources

**In Code (Javadoc)**:
```java
// Look for @see tags pointing to related classes
// Example: @see PaymentRetryScheduler
```

**In Documentation**:
- `PAYMENT_IMPROVEMENTS.md` - Deep dive on each feature
- `PAYMENT_ARCHITECTURE.md` - System design & flows
- `PAYMENT_IMPLEMENTATION_GUIDE.md` - How to implement

**In Configuration**:
```properties
# Each property has meaningful name
payment.checkout-session-timeout-ms
payment.max-retry-attempts
# etc.
```

---

## Quick Links

- 📖 Implementation Guide: `/docs/PAYMENT_IMPLEMENTATION_GUIDE.md`
- 🏗️ Architecture: `/docs/PAYMENT_ARCHITECTURE.md`
- 📚 Full Details: `/docs/PAYMENT_IMPROVEMENTS.md`
- 📊 Summary: `/docs/PAYMENT_SUMMARY.md`
- 🔧 Code: `/src/main/java/com/api/e_commerce/payment/`

---

## Version Info

```
Payment Service Version: 1.0
Release Date: May 22, 2026
Status: Production Ready ✅
Java: 21+
Spring Boot: 3.5.12+
```

---

**Last Updated**: May 22, 2026  
**Maintainer**: Payment Service Team  
**Next Review**: When features added  


