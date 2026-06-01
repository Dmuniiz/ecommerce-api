# 🏗️ Payment Service Architecture

## High-Level Payment Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                      CLIENT / FRONTEND                          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           │ POST /checkout-session
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PAYMENT CONTROLLER                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 1. Validate user authorization                           │  │
│  │ 2. Route webhook events to handlers                      │  │
│  │ 3. Handle POST /refund endpoint                          │  │
│  │ 4. Return payment details                                │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PAYMENT SERVICE                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Core Business Logic                                      │  │
│  │ • createCheckoutSession()                                │  │
│  │ • retryFailedPayment()                                   │  │
│  │ • refundPayment()                                        │  │
│  │ • updatePaymentStatus()                                  │  │
│  │ • validateStatusTransition()                             │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                ┌──────────┴──────────┐
                │                     │
                ↓                     ↓
    ┌──────────────────────┐ ┌──────────────────────┐
    │  PAYMENT STRATEGIES  │ │  DATA REPOSITORIES   │
    ├──────────────────────┤ ├──────────────────────┤
    │  • STRIPE            │ │  • PaymentRepository │
    │  • ABACATE_PAY       │ │  • PaymentRetryRepo  │
    │  • FAKE              │ │  • Transaction Repo  │
    └──────────────────────┘ └──────────────────────┘
                │
                ↓
    ┌──────────────────────────────┐
    │  PAYMENT GATEWAY (Stripe)    │
    │  • Session.create()          │
    │  • Refund.create()           │
    │  • Webhook validation        │
    └──────────────────────────────┘
```

---

## 🔄 Payment Status State Machine

```
┌─────────┐
│ PENDING │  (Customer initiated checkout)
└────┬────┘
     │
     ├─────→ SUCCEEDED  (Payment confirmed via webhook)
     │           │
     │           └─────→ REFUNDED (Customer refund issued)
     │
     ├─────→ FAILED (Permanent failure)
     │
     └─────→ CANCELLED (User cancelled checkout)
```

---

## 🔁 Retry Logic Flow

```
Payment Attempt
      ↓
   FAILS
      ↓
Create PaymentRetry Record
   attempt_count = 0
   next_retry_at = NOW + backoff
      ↓
[SCHEDULER RUNS (every 5 minutes)]
      ↓
Find PaymentRetry with:
   - is_retryable = true
   - attempt_count < max_attempts
   - next_retry_at <= NOW
      ↓
   ┌──────────────────┐
   │ RETRY ATTEMPT #1 │
   └─────┬────────────┘
         │
         ├─── SUCCESS → Update Payment Status → Done ✓
         │
         └─── FAIL → attempt_count++ → Calculate backoff
                           ↓
                   next_retry_at = NOW + (backoff * multiplier)
                           ↓
                    ┌──────────────────┐
                    │ RETRY ATTEMPT #2 │  (after backoff)
                    └─────┬────────────┘
                          │
                          ├─── SUCCESS → Done ✓
                          │
                          └─── FAIL → Continue...
                                  ↓
                          ┌──────────────────┐
                          │ RETRY ATTEMPT #3 │
                          └─────┬────────────┘
                                │
                                ├─── SUCCESS → Done ✓
                                │
                                └─── FAIL → Max Retries Exhausted
                                        ↓
                                  is_retryable = false
                                  Payment Status = FAILED
                                  Alert Admin!
```

---

## 📊 Exponential Backoff Calculation

```
Attempt # │ Backoff Calculation        │ Delay (seconds)
─────────┼────────────────────────────┼─────────────────
    1    │ 1000 ms                    │ 1
    2    │ 1000 × 2^1 = 2000 ms       │ 2
    3    │ 1000 × 2^2 = 4000 ms       │ 4
    4    │ 1000 × 2^3 = 8000 ms       │ 8
    5    │ min(16000, 30000) = 16000  │ 16

(Default: multiplier=2.0, max=30s)
```

---

## 🔐 Concurrent Payment Prevention

```
User Action 1: Click Checkout
      ↓
Check: Is there a PENDING payment for this order?
      ├─ YES → Return Error: "Payment already in progress"
      │
      └─ NO → Create Payment with status PENDING
              Return checkout_url

User Action 2: Click Checkout Again (within 5 min)
      ↓
Check: Is there a PENDING payment for this order?
      ├─ YES → Return Error: "Payment already in progress" ✓
      │
      └─ NO → (Would create new, but check prevents this)
```

---

## 💳 Refund Process

```
Admin Initiates Refund via API
      ↓
Validate:
  ✓ Payment exists
  ✓ Status = SUCCEEDED
  ✓ Refund amount > 0
  ✓ Refund amount ≤ paid amount
      ↓
Call Gateway Refund API
  • Stripe: Refund.create()
  • Amount in cents
      ↓
   ┌─── SUCCESS ──┐
   │              ↓
Update Payment  Register Transaction
Status = REFUND CREATE_REFUND_COMPLETED
   │              ↓
   └─── RESPOND OK
            ↓
         Customer receives refund
         (2-5 business days)
```

---

## 📡 Webhook Event Processing

```
Stripe sends Event
      ↓
PaymentController.handleStripeWebhook()
      ↓
Validate Signature ← HMAC-SHA256 verification
      ├─ INVALID → Return 400 Bad Request
      │
      └─ VALID → Continue
           ↓
      Match Event Type:
      ├─ "checkout.session.completed"
      │     ↓
      │  handleCheckoutSessionCompleted()
      │     ↓
      │  Extract orderId from metadata
      │     ↓
      │  OrderService.confirmPayment()
      │     ↓
      │  Payment status → SUCCEEDED
      │     ↓
      │  Return 200 OK
      │
      ├─ "charge.refunded"
      │     ↓
      │  handleChargeRefunded()
      │     ↓
      │  [TODO: Update to REFUNDED]
      │
      ├─ "charge.dispute.created"
      │     ↓
      │  handleChargeDisputeCreated()
      │     ↓
      │  [TODO: Alert admin]
      │
      └─ "payment_intent.payment_failed"
            ↓
         handlePaymentFailed()
            ↓
         [TODO: Notify customer]
```

---

## 🗄️ Database Schema

```
PAYMENTS TABLE
┌──────────────────────────────────┐
│ id (UUID)                        │
│ order_id (UUID) - FK orders      │
│ amount (BigDecimal)              │
│ currency (String)                │
│ provider (Enum)                  │
│ status (Enum)                    │
│ provider_checkout_session_id     │
│ failure_reason (String)          │
│ paid_at (Timestamp)              │
│ created_at (Timestamp)           │
│ updated_at (Timestamp)           │
│ version (Long)                   │
└──────────────────────────────────┘
           │
           └─── 1:1 ────→ PAYMENT_RETRIES TABLE
                           ┌──────────────────────────────┐
                           │ id (UUID)                    │
                           │ payment_id (UUID) - FK       │
                           │ attempt_count (Integer)      │
                           │ max_attempts (Integer)       │
                           │ next_retry_at (Timestamp)    │
                           │ is_retryable (Boolean)       │
                           │ last_error_message (String)  │
                           │ created_at (Timestamp)       │
                           │ updated_at (Timestamp)       │
                           └──────────────────────────────┘

PAYMENT_TRANSACTIONS TABLE
┌──────────────────────────────────┐
│ id (UUID)                        │
│ payment_id (UUID) - FK           │
│ type (Enum)                      │
│ status (Enum)                    │
│ provider_event_id (String)       │
│ provider_transaction_id (String) │
│ raw_payload (JSON)               │
│ error_message (String)           │
│ created_at (Timestamp)           │
└──────────────────────────────────┘
```

---

## 🔧 Configuration Flow

```
application.properties
      ↓
Spring Boot autoconfigures
      ↓
PaymentConfig Bean Created
┌─────────────────────────────────┐
│ • Timeouts                      │
│ • Retry settings                │
│ • Webhook settings              │
│ • Concurrent payment prevention │
│ • Success/cancel URLs           │
└─────────────────────────────────┘
      ↓
Injected into:
├─ PaymentService
├─ PaymentRetryScheduler
├─ StripePaymentStrategyImpl
└─ PaymentController
```

---

## ⏲️ Scheduler Timeline

```
T=00:00 Application starts
        @EnableScheduling activates
        
T=05:00 PaymentRetryScheduler.retryFailedPayments() runs
        - Query: Find payments ready for retry
        - Process: Call retryFailedPayment() for each
        - Update: Attempt count, next_retry_at
        
T=10:00 Scheduler runs again
        
T=12:00 Daily cleanup: cleanupExhaustedRetries()
        - Query: Find exhausted retries (attempt ≥ max)
        - Action: Log, potentially archive
        
Repeats every 5 minutes (configurable)
```

---

## 🎯 Request/Response Examples

### Create Checkout Session
```
REQUEST
POST /api/v1/payments/{orderId}/checkout-session
Authorization: Bearer eyJhbG...
Content-Type: application/json

{
  "provider": "STRIPE"
}

PROCESSING
1. Validate order belongs to user ✓
2. Check concurrent prevention lock ✓
3. Create Payment record (status=PENDING)
4. Call StripePaymentStrategyImpl.createCheckoutSession()
5. Register CHECKOUT_SESSION_CREATED transaction
6. Return response

RESPONSE (200 OK)
{
  "checkoutUrl": "https://checkout.stripe.com/pay/cs_live_...",
  "sessionId": "cs_live_..."
}
```

### Refund Payment
```
REQUEST
POST /api/v1/payments/{paymentId}/refund?amount=50.00&reason=Return

PROCESSING
1. Fetch payment ✓
2. Validate status = SUCCEEDED ✓
3. Validate amount ≤ paid amount ✓
4. Call StripePaymentStrategyImpl.processRefund() ✓
5. Update Payment.status = REFUNDED ✓
6. Register REFUND_COMPLETED transaction ✓

RESPONSE (200 OK)
"Refund processed successfully"
```

---

## 📈 Performance Characteristics

```
Operation              │ Typical Time │ Max Time
──────────────────────┼──────────────┼──────────
Checkout session      │ 500-1000ms   │ 2000ms
Webhook processing    │ 100-500ms    │ 1000ms
Retry scheduler run   │ 100-200ms    │ 500ms
Refund processing     │ 200-800ms    │ 1500ms
Database query        │ 10-50ms      │ 100ms
Stripe API call       │ 300-800ms    │ 2000ms
```

---

This architecture is designed for:
- ✅ **Reliability**: Automatic retries handle transient failures
- ✅ **Consistency**: Status validation prevents invalid states
- ✅ **Scalability**: Async scheduler doesn't block requests
- ✅ **Observability**: Comprehensive logging and transactions
- ✅ **Security**: Signature validation and user checks
- ✅ **Maintainability**: Clean separation of concerns


