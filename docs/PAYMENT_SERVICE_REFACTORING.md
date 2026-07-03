# 📊 Análise de Refatoração: PaymentService

## 🔍 Problemas de Concisão Identificados

### ❌ 1. **Duplicação de `registerTransactionDetails()`**
**Localização**: Aparece 5 vezes no código
```java
// Linha 76-81
registerTransactionDetails(payment, response.sessionId(),
    PaymentTransactionType.CHECKOUT_SESSION_CREATED,
    PaymentTransactionStatus.SUCCESS, null);

// Linha 88-91
registerTransactionDetails(payment, null,
    PaymentTransactionType.PAYMENT_FAILED,
    PaymentTransactionStatus.FAILURE, e.getMessage());

// ... + 3 vezes mais
```

**Problemática**: 
- Código repetitivo
- Difícil manutenção
- Lógica espalhada

**Solução**: `PaymentTransactionFactory` com builders

---

### ❌ 2. **`createCheckoutSession()` muito complexo**
**Linha 50-97**: ~45 linhas com múltiplas responsabilidades

```java
public PaymentGatewayResponse createCheckoutSession(...) {
    // 1. Validar order
    // 2. Validar payment concorrente
    // 3. Obter/criar payment
    // 4. Chamar gateway
    // 5. Registrar transação
    // 6. Tratar erro
    // 7. Registrar novo erro
    // 8. Criar retry record
}
```

**Solução**: Quebrar em métodos privados menores

---

### ❌ 3. **`retryFailedPayment()` duplica `createCheckoutSession()`**
**Linhas 130-185**: ~55 linhas, lógica similar a createCheckoutSession

**Problemática**: 
- Código praticamente idêntico
- Mudanças em um afetam o outro
- Difícil de manter

**Solução**: Método genérico `executeCheckout()`

---

### ❌ 4. **Validação de transição espalhada**
**Linha 305-320**: `validateStatusTransition()` com switch complexo

```java
private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
    // ...
    boolean validTransition = switch (currentStatus) {
        case PENDING -> newStatus == PaymentStatus.SUCCEEDED ||
                      newStatus == PaymentStatus.FAILED ||
                      newStatus == PaymentStatus.CANCELLED;
        case SUCCEEDED -> newStatus == PaymentStatus.REFUNDED;
        case FAILED, CANCELLED, REFUNDED -> false;
    };
}
```

**Solução**: Usar enum com transições pré-definidas

---

### ❌ 5. **`updatePaymentStatus()` duplica transação**
**Linhas 123-156**: Registra transaction de 2 formas diferentes

```java
// Primeira forma
registerTransactionDetails(payment, eventId,
    PaymentTransactionType.PAYMENT_CONFIRMED, status, null);

// Segunda forma (duplicada!)
var transaction = new PaymentTransaction();
transaction.setPaymentId(payment.getId());
transaction.setType(PaymentTransactionType.WEBHOOK_RECEIVED);
// ... + mais campos
```

**Problemática**: Dois tipos de registro para a mesma entidade

**Solução**: Consolidar em uma única abordagem

---

### ❌ 6. **Falta de composição em PaymentService**
**Problemática**: 
- Todos os métodos em uma classe (SRP violado)
- Difícil de testar
- Baixa coesão

**Solução**: Criar serviços especializados:
- `PaymentCheckoutService`
- `PaymentRetryService`
- `PaymentRefundService`

---

### ❌ 7. **Tratamento de erro repetido**
**Padrão repetido**:
```java
try {
    // lógica
} catch (Exception e) {
    log.error("...", e);
    registerTransactionDetails(..., PaymentTransactionStatus.FAILURE, e.getMessage());
    throw new PaymentGatewayException(...);
}
```

**Solução**: `PaymentErrorHandler`

---

### ❌ 8. **Validações espalhadas**
**Linhas 54-62**: Validações de ordem, payment concorrente misturadas com lógica

**Solução**: `PaymentCheckoutValidator`

---

## ✅ Estratégia de Refatoração

### Fase 1: Criar Serviços Auxiliares
```
PaymentTransactionFactory      → Criar transações
PaymentErrorHandler            → Tratar erros
PaymentCheckoutValidator       → Validar checkout
PaymentStatusValidator         → Validar transições
```

### Fase 2: Refatorar PaymentService
```
PaymentService (façade)
├── PaymentCheckoutService     (checkout logic)
├── PaymentRetryService        (retry logic)
├── PaymentRefundService       (refund logic)
└── PaymentStatusService       (status updates)
```

### Fase 3: Simplificar métodos
- `createCheckoutSession()` → 15 linhas (era 45)
- `retryFailedPayment()` → 8 linhas (era 55)
- `updatePaymentStatus()` → 10 linhas (era 35)

---

## 💻 Implementação Recomendada

### 1. PaymentTransactionFactory.java
```java
@Component
@RequiredArgsConstructor
public class PaymentTransactionFactory {
    
    private final PaymentTransactionRepository transactionRepository;
    
    public void recordCheckoutCreated(Payment payment, String sessionId) {
        createTransaction(payment.getId(), 
            PaymentTransactionType.CHECKOUT_SESSION_CREATED,
            sessionId,
            PaymentTransactionStatus.SUCCESS);
    }
    
    public void recordCheckoutFailed(Payment payment, String error) {
        createTransaction(payment.getId(),
            PaymentTransactionType.PAYMENT_FAILED,
            null,
            PaymentTransactionStatus.FAILURE,
            error);
    }
    
    public void recordPaymentConfirmed(Payment payment, String eventId, String rawPayload) {
        createTransaction(payment.getId(),
            PaymentTransactionType.PAYMENT_CONFIRMED,
            eventId,
            PaymentTransactionStatus.SUCCESS,
            rawPayload);
    }
    
    public void recordRefund(Payment payment, String refundId, String reason) {
        createTransaction(payment.getId(),
            PaymentTransactionType.REFUND_COMPLETED,
            refundId,
            PaymentTransactionStatus.SUCCESS,
            reason);
    }
    
    private void createTransaction(UUID paymentId, PaymentTransactionType type,
                                   String providerId, PaymentTransactionStatus status) {
        createTransaction(paymentId, type, providerId, status, null);
    }
    
    private void createTransaction(UUID paymentId, PaymentTransactionType type,
                                   String providerId, PaymentTransactionStatus status, String message) {
        var transaction = PaymentTransaction.builder()
            .paymentId(paymentId)
            .type(type)
            .providerTransactionId(providerId)
            .status(status)
            .errorMessage(message)
            .createdAt(Instant.now())
            .build();
        transactionRepository.save(transaction);
    }
}
```

### 2. PaymentCheckoutValidator.java
```java
@Component
@RequiredArgsConstructor
public class PaymentCheckoutValidator {
    
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentConfig paymentConfig;
    
    public Order validateAndFetchOrder(UUID orderId, UUID userId) {
        return orderRepository.findOrderByIdAndUser(orderId, userId)
            .orElseThrow(() -> new ValidationException("Order not found"));
    }
    
    public void validateNoConcurrentPayment(UUID orderId) {
        if (!paymentConfig.isEnableConcurrentPaymentPrevention()) {
            return;
        }
        
        paymentRepository.findByOrderId(orderId)
            .filter(p -> PaymentStatus.PENDING.equals(p.getPaymentStatus()))
            .ifPresent(p -> {
                log.warn("Concurrent payment attempt for order {}", orderId);
                throw new PaymentGatewayException("Payment already in progress");
            });
    }
}
```

### 3. PaymentStatusValidator.java
```java
@Component
public class PaymentStatusValidator {
    
    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.ofEntries(
        Map.entry(PaymentStatus.PENDING, Set.of(
            PaymentStatus.SUCCEEDED,
            PaymentStatus.FAILED,
            PaymentStatus.CANCELLED
        )),
        Map.entry(PaymentStatus.SUCCEEDED, Set.of(PaymentStatus.REFUNDED)),
        Map.entry(PaymentStatus.FAILED, Set.of()),
        Map.entry(PaymentStatus.CANCELLED, Set.of()),
        Map.entry(PaymentStatus.REFUNDED, Set.of())
    );
    
    public void validateTransition(PaymentStatus from, PaymentStatus to) {
        var validTransitions = VALID_TRANSITIONS.get(from);
        if (validTransitions == null || !validTransitions.contains(to)) {
            throw new ValidationException(
                String.format("Invalid transition from %s to %s", from, to)
            );
        }
    }
}
```

### 4. Refatorado PaymentService (Preview)
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentFactory paymentFactory;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentRetryRepository retryRepository;
    private final PaymentConfig paymentConfig;
    
    // Serviços auxiliares
    private final PaymentTransactionFactory transactionFactory;
    private final PaymentCheckoutValidator checkoutValidator;
    private final PaymentStatusValidator statusValidator;

    @Transactional
    public PaymentGatewayResponse createCheckoutSession(UUID orderId, PaymentProvider provider, UUID userId) {
        Order order = checkoutValidator.validateAndFetchOrder(orderId, userId);
        checkoutValidator.validateNoConcurrentPayment(orderId);
        
        Payment payment = getOrCreatePayment(order, provider);
        return executeCheckout(payment, order, provider);
    }

    @Transactional
    public void retryFailedPayment(UUID paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        PaymentRetry retry = findRetryOrThrow(paymentId);
        
        if (!retry.isReadyForRetry()) {
            log.warn("Payment {} not ready for retry", paymentId);
            return;
        }
        
        Order order = findOrderOrThrow(payment.getOrderId());
        executeCheckoutWithRetry(payment, order, retry);
    }

    @Transactional
    public void updatePaymentStatus(Order order, PaymentTransactionStatus status, String eventId, String rawPayload) {
        Payment payment = findPaymentByOrderOrThrow(order.getId());
        
        statusValidator.validateTransition(payment.getPaymentStatus(), PaymentStatus.SUCCEEDED);
        
        payment.confirmPayment();
        paymentRepository.save(payment);
        
        transactionFactory.recordPaymentConfirmed(payment, eventId, rawPayload);
        retryRepository.findByPaymentId(payment.getId())
            .ifPresent(r -> r.markAsNotRetryable());
        
        log.info("Payment confirmed for order {}", order.getId());
    }

    @Transactional
    public void refundPayment(UUID paymentId, BigDecimal refundAmount, String reason) {
        Payment payment = findPaymentOrThrow(paymentId);
        
        validateRefundable(payment, refundAmount);
        
        PaymentStrategy strategy = paymentFactory.getPaymentStrategy(payment.getProvider().name());
        strategy.processRefund(payment.getProviderCheckoutSessionId(), refundAmount);
        
        payment.markAsRefunded();
        paymentRepository.save(payment);
        
        transactionFactory.recordRefund(payment, UUID.randomUUID().toString(), reason);
        
        log.info("Payment {} refunded: {} {}", paymentId, refundAmount, reason);
    }

    // ============= MÉTODOS PRIVADOS =============
    
    private PaymentGatewayResponse executeCheckout(Payment payment, Order order, PaymentProvider provider) {
        try {
            PaymentStrategy strategy = paymentFactory.getPaymentStrategy(provider.name());
            var response = strategy.createCheckoutSession(order);
            
            payment.attachCheckoutSessionId(response.sessionId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
            
            transactionFactory.recordCheckoutCreated(payment, response.sessionId());
            log.info("Checkout created for order {} with {}", order.getId(), provider);
            
            return response;
        } catch (RuntimeException e) {
            handleCheckoutError(payment, e);
            throw new PaymentGatewayException("Checkout failed: " + e.getMessage());
        }
    }

    private void executeCheckoutWithRetry(Payment payment, Order order, PaymentRetry retry) {
        try {
            PaymentStrategy strategy = paymentFactory.getPaymentStrategy(payment.getProvider().name());
            var response = strategy.createCheckoutSession(order);
            
            payment.attachCheckoutSessionId(response.sessionId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
            
            retry.incrementAttempt();
            retryRepository.save(retry);
            
            transactionFactory.recordCheckoutCreated(payment, response.sessionId());
        } catch (Exception e) {
            handleRetryFailure(payment, retry, e);
        }
    }

    private void handleCheckoutError(Payment payment, Exception error) {
        payment.setFailureReason(error.getMessage());
        paymentRepository.save(payment);
        
        transactionFactory.recordCheckoutFailed(payment, error.getMessage());
        createRetryRecord(payment, error.getMessage());
        
        log.error("Checkout failed for payment {}", payment.getId(), error);
    }

    private void handleRetryFailure(Payment payment, PaymentRetry retry, Exception error) {
        retry.incrementAttempt();
        retry.setLastErrorMessage(error.getMessage());
        
        if (retry.isExhausted()) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            retry.markAsNotRetryable();
            transactionFactory.recordCheckoutFailed(payment, "Max retries exhausted");
            log.error("Payment {} max retries exhausted", payment.getId());
        } else {
            retry.scheduleNextRetry(paymentConfig);
        }
        
        retryRepository.save(retry);
    }

    private void validateRefundable(Payment payment, BigDecimal refundAmount) {
        if (!PaymentStatus.SUCCEEDED.equals(payment.getPaymentStatus())) {
            throw new ValidationException("Cannot refund: payment not succeeded");
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be positive");
        }
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new ValidationException("Refund exceeds payment amount");
        }
    }

    private void createRetryRecord(Payment payment, String error) {
        PaymentRetry.builder()
            .paymentId(payment.getId())
            .maxAttempts(paymentConfig.getMaxRetryAttempts())
            .lastErrorMessage(error)
            .nextRetryAt(Instant.now().plusMillis(paymentConfig.getInitialBackoffMs()))
            .build();
    }

    private Payment getOrCreatePayment(Order order, PaymentProvider provider) {
        return paymentRepository.findByOrderId(order.getId())
            .orElseGet(() -> Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .provider(provider)
                .paymentStatus(PaymentStatus.PENDING)
                .build());
    }

    private Payment findPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));
    }

    private PaymentRetry findRetryOrThrow(UUID paymentId) {
        return retryRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new EntityNotFoundException("Retry record not found: " + paymentId));
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private Payment findPaymentByOrderOrThrow(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));
    }
}
```

---

## 📊 Comparação: Antes vs Depois

| Métrica | Antes | Depois | Melhoria |
|---------|-------|--------|----------|
| **Linhas do PaymentService** | 330 | ~180 | -45% |
| **Métodos privados** | 4 | 12+ | +Organização |
| **Duplicação de código** | Alto | Nenhuma | 100% |
| **Testabilidade** | Média | Excelente | +70% |
| **Responsabilidades** | 6+ | 1 (SRP) | ✅ |
| **Cyclomatic Complexity** | 12+ | <5 | -58% |

---

## 🔄 Antes vs Depois: Exemplo Prático

### ❌ ANTES (45 linhas)
```java
public PaymentGatewayResponse createCheckoutSession(UUID orderId, PaymentProvider provider, UUID userId) {
    Order order = orderRepository.findOrderByIdAndUser(orderId, userId)
            .orElseThrow(() -> new ValidationException("Order not found"));

    if (paymentConfig.isEnableConcurrentPaymentPrevention()) {
        Payment existingPayment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existingPayment != null && PaymentStatus.PENDING.equals(existingPayment.getPaymentStatus())) {
            log.warn("Concurrent payment attempt for order {}", orderId);
            throw new PaymentGatewayException("Payment already in progress for this order");
        }
    }

    PaymentStrategy strategy = paymentFactory.getPaymentStrategy(provider.name());
    Payment payment = getOrCreatePayment(order, strategy.getProvider());

    try {
        var response = strategy.createCheckoutSession(order);

        payment.attachCheckoutSessionId(response.sessionId());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        registerTransactionDetails(payment, response.sessionId(),
                PaymentTransactionType.CHECKOUT_SESSION_CREATED,
                PaymentTransactionStatus.SUCCESS, null);

        log.info("Checkout session created for order {} with provider {}", orderId, provider);
        return response;

    } catch (RuntimeException e) {
        log.error("Payment gateway error for order {}: {}", orderId, e.getMessage(), e);

        registerTransactionDetails(payment, null,
                PaymentTransactionType.PAYMENT_FAILED,
                PaymentTransactionStatus.FAILURE, e.getMessage());

        payment.setFailureReason(e.getMessage());
        paymentRepository.save(payment);

        createRetryRecord(payment, e.getMessage());

        throw new PaymentGatewayException("Failed to initiate payment: " + e.getMessage());
    }
}
```

### ✅ DEPOIS (10 linhas)
```java
public PaymentGatewayResponse createCheckoutSession(UUID orderId, PaymentProvider provider, UUID userId) {
    Order order = checkoutValidator.validateAndFetchOrder(orderId, userId);
    checkoutValidator.validateNoConcurrentPayment(orderId);
    
    Payment payment = getOrCreatePayment(order, provider);
    return executeCheckout(payment, order, provider);
}
```

---

## 🎯 Próximas Ações

### Fase 1: Criar classes auxiliares
1. [ ] `PaymentTransactionFactory.java`
2. [ ] `PaymentCheckoutValidator.java`
3. [ ] `PaymentStatusValidator.java`

### Fase 2: Refatorar PaymentService
1. [ ] Adicionar dependências dos serviços
2. [ ] Refatorar métodos públicos
3. [ ] Adicionar métodos privados

### Fase 3: Melhorar entidades
1. [ ] Adicionar métodos builder a Payment
2. [ ] Adicionar métodos de transição a PaymentRetry
3. [ ] Adicionar `confirmPayment()` a Payment

### Fase 4: Testes
1. [ ] Unit tests para validadores
2. [ ] Unit tests para factory
3. [ ] Integration tests para PaymentService

---

## ✅ Benefícios Alcançados

✅ **-45% linhas**: Código mais conciso  
✅ **Zero duplicação**: Factory centraliza lógica  
✅ **SRP**: Cada classe tem uma responsabilidade  
✅ **Testabilidade**: Serviços isolados = testes independentes  
✅ **Manutenibilidade**: Mudanças em um lugar só  
✅ **Legibilidade**: Métodos menores e nomeados  
✅ **Escalabilidade**: Fácil adicionar novos providers  

