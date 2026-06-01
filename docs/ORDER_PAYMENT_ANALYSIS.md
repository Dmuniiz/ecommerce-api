    # 📋 Análise: Coesão Order-Payment

## 🔍 Problemas Identificados

### ❌ 1. **Estados desalinhados**
**Problemática**:
```
OrderStatus:   PENDING, PAID, SHIPPED, DELIVERED, CANCELLED (5 estados)
PaymentStatus: PENDING, SUCCEEDED, FAILED, CANCELLED, REFUNDED (5 estados)

ISSUE: Order.PENDING ≠ Payment.PENDING (tempos diferentes)
       Order.PAID ≠ Payment.SUCCEEDED (nomes diferentes)
```

**Impacto**: Confusão nos estados, lógica complexa de mapeamento

---

### ❌ 2. **Falta Payment Reference na Order**
```java
// Order.java - FALTANDO
private UUID paymentId;  // ← Não existe!
```

**Problemática**: Não há relação direta entre Order e Payment
**Impacto**: Queries complexas para encontrar payment de um order

---

### ❌ 3. **Sem tracking de Updated Timestamp**
```java
@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt = Instant.now();
// FALTANDO: updated_at
```

**Problemática**: Não conseguir saber quando um order foi actualizado
**Impacto**: Problemas de auditoria e troubleshooting

---

### ❌ 4. **Sem Optimistic Locking**
```java
// FALTANDO @Version
private Long version;
```

**Problemática**: Sem proteção contra race conditions
**Impacto**: Possível estado inconsistente em operações paralelas

---

### ❌ 5. **Status Transitions Frágil**
```java
public void markAsPaid() {
    if(this.status != OrderStatus.PENDING) {
        throw new IllegalStateException(...); // Genérico
    }
    this.status = OrderStatus.PAID;
}
```

**Problemática**: 
- Sem validação de transições válidas
- Sem synchronization com PaymentStatus
- Sem logging

---

### ❌ 6. **Sem Payment Failure Handling**
**Problemática**: Não há método para processar falha de pagamento
```java
// FALTANDO: handlePaymentFailure()
```

**Impacto**: Ordem fica em estado inconsistente se pagamento falha

---

### ❌ 7. **OrderStatus.PENDING é ambíguo**
```
Pode significar:
- Order criado (ainda sem payment)
- Payment em processamento
- Payment falhou e aguarda retry
```

**Mais claro seria**: `PENDING_PAYMENT`, `WAITING_PAYMENT`, etc.

---

### ❌ 8. **Sem Payment Retry Integration**
**Problemática**: Order não sabe que payment foi retried
**Impacto**: Sem informação sobre tentativas de checkout

---

### ❌ 9. **Sem Refund Tracking**
**Problemática**: Não há forma de saber se order foi refundado
**Impacto**: Sem suporte a refunds completo

---

### ❌ 10. **OrderService.confirmPayment() direto demais**
```java
@Transactional
public void confirmPayment(UUID orderId, String eventId, String rawPayload) {
    // Tudo em um método - sem validação
    // Sem retry logic
    // Sem transaction logging
}
```

**Problemática**: Falta de resiliência e validação
**Impacto**: Webhook failure = order inconsistente

---

## 🎯 Melhorias Recomendadas

### 1. **Alinhamento de Estados** [PRIORITY: CRÍTICA]
```
Novo OrderStatus:
- CREATED              (order criado, cart não limpo)
- PENDING_PAYMENT      (aguardando confirmação de pagamento) ✨
- PAID                 (pagamento confirmado)
- PAYMENT_FAILED       (pagamento falhou - em retry) ✨
- SHIPPED              (despachado)
- DELIVERED            (entregue)
- CANCELLED            (cancelado)
- REFUNDED             (refundado) ✨
```

**Mapping com PaymentStatus**:
```
Order.PENDING_PAYMENT  ← Payment.PENDING
Order.PAID             ← Payment.SUCCEEDED
Order.PAYMENT_FAILED   ← Payment.FAILED
Order.REFUNDED         ← Payment.REFUNDED
Order.CANCELLED        ← Payment.CANCELLED
```

---

### 2. **Payment Reference em Order** [PRIORITY: CRÍTICA]
```java
@Column(name = "payment_id", unique = true)
private UUID paymentId;  // ← ADD THIS

@Column(name = "payment_status_snapshot")
@Enumerated(EnumType.STRING)
private PaymentStatus paymentStatusSnapshot;  // ← Cache para query rápida
```

---

### 3. **Audit Timestamps** [PRIORITY: ALTA]
```java
@Column(name = "updated_at")
private Instant updatedAt;

@Column(name = "paid_at")
private Instant paidAt;

@Column(name = "shipped_at")
private Instant shippedAt;

@PreUpdate
public void preUpdate() {
    this.updatedAt = Instant.now();
}
```

---

### 4. **Optimistic Locking** [PRIORITY: ALTA]
```java
@Version
private Long version;  // ← Previne race conditions
```

---

### 5. **Enhanced Status Management** [PRIORITY: ALTA]
```java
public void confirmPayment(PaymentStatus paymentStatus) {
    validateTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
    this.status = OrderStatus.PAID;
    this.paidAt = Instant.now();
    this.paymentStatusSnapshot = paymentStatus;
}

public void markPaymentFailed(String reason) {
    validateTransition(status, OrderStatus.PAYMENT_FAILED);
    this.status = OrderStatus.PAYMENT_FAILED;
    this.paymentFailureReason = reason;
}

private void validateTransition(OrderStatus from, OrderStatus to) {
    // Implementar state machine
}
```

---

### 6. **Order Event Tracking** [PRIORITY: MÉDIA]
```java
@ElementCollection(fetch = FetchType.LAZY)
@CollectionTable(name = "order_events")
private List<OrderEvent> events = new ArrayList<>();

// Rastrear o que aconteceu
public void recordPaymentAttempt(String provider, boolean success) { }
public void recordPaymentRefund(BigDecimal amount) { }
```

---

### 7. **Payment Sync** [PRIORITY: ALTA]
```java
/**
 * Sincroniza status do payment com order
 * Chamado por PaymentService quando payment muda
 */
public void syncPaymentStatus(PaymentStatus newPaymentStatus, UUID paymentId) {
    this.paymentId = paymentId;
    this.paymentStatusSnapshot = newPaymentStatus;
    
    // Atualizar OrderStatus baseado no Payment
    OrderStatus newOrderStatus = mapPaymentStatusToOrderStatus(newPaymentStatus);
    validateTransition(this.status, newOrderStatus);
    this.status = newOrderStatus;
}

private OrderStatus mapPaymentStatusToOrderStatus(PaymentStatus paymentStatus) {
    return switch(paymentStatus) {
        case PENDING -> OrderStatus.PENDING_PAYMENT;
        case SUCCEEDED -> OrderStatus.PAID;
        case FAILED -> OrderStatus.PAYMENT_FAILED;
        case REFUNDED -> OrderStatus.REFUNDED;
        case CANCELLED -> OrderStatus.CANCELLED;
    };
}
```

---

## 📊 Comparação: Antes vs Depois

```
ANTES:
Order.status = PENDING
↓ (ambíguo - significa o quê?)
No payment reference
↓
Complex queries para encontrar payment
↓
Sem updated_at
↓
Sem controle de concorrência
↓
confirmPayment() sem validação


DEPOIS:
Order.status = PENDING_PAYMENT
Order.paymentId = uuid-123
Order.paymentStatusSnapshot = PENDING
↓
Order.updatedAt = timestamp
↓
Order.version = 2 (optimistic lock)
↓
syncPaymentStatus() valida estado
↓
Completo rastreamento de eventos
```

---

## 🔗 Relacionamentos Propostos

### Antes:
```
Order -----> OrderItems
Order -----> Addresses (embedded)
         (sem link direto para Payment)
```

### Depois:
```
Order -----> Payment (new relation!)
          ↓
        paymentId (column)
        paymentStatusSnapshot (cache)
        paidAt (timestamp)

Order -----> OrderEvents (new!)
          ↓
        payment_attempt_1
        payment_attempt_2 (retry)
        payment_confirmed
        refund_initiated
```

---

## 💻 Código Implementação

### OrderStatus.java (NOVO)
```java
public enum OrderStatus {
    CREATED,           // Ordem criada, payment ainda não iniciado
    PENDING_PAYMENT,   // Aguardando confirmação de pagamento (Payment.PENDING)
    PAID,              // Pagamento confirmado (Payment.SUCCEEDED)
    PAYMENT_FAILED,    // Pagamento falhou - em fila de retry (Payment.FAILED)
    SHIPPED,           // Despachado para cliente
    DELIVERED,         // Entregue ao cliente
    CANCELLED,         // Cancelado antes do pagamento
    REFUNDED          // Refundado (Payment.REFUNDED)
}
```

### Order.java (MELHORADO)
```java
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_id", columnList = "payment_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class Order {
    // ... existing fields ...
    
    @Column(name = "payment_id", unique = true)
    private UUID paymentId;
    
    @Column(name = "payment_status_snapshot")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatusSnapshot;
    
    @Column(name = "paid_at")
    private Instant paidAt;
    
    @Column(name = "shipped_at")
    private Instant shippedAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "payment_failure_reason")
    private String paymentFailureReason;
    
    @Version
    private Long version;
    
    // ... methods ...
}
```

---

## 📈 Implementação Passo-a-Passo

### FASE 1: Database (Migration V16)
- [ ] Add columns a orders table
- [ ] Add payment_id foreign key
- [ ] Add indexes

### FASE 2: Code Updates
- [ ] Update OrderStatus enum
- [ ] Update Order entity
- [ ] Add status validation methods
- [ ] Add sync methods

### FASE 3: Integration
- [ ] Update OrderService
- [ ] Update PaymentService
- [ ] Update webhooks

### FASE 4: Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] End-to-end tests

---

## 🎯 Benefícios

✅ **Estados claros**: PENDING_PAYMENT deixa evidente o que está acontecendo  
✅ **Direct Payment Link**: paymentId permite queries rápidas  
✅ **Audit Trail**: timestamps + version para compliance  
✅ **Race Condition Safe**: @Version previne conflitos  
✅ **Event Tracking**: Histórico completo de transações  
✅ **Resilient**: Status sync valida transições  
✅ **Maintainable**: Código mais limpo e fácil de entender  


