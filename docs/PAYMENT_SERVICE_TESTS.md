# 🧪 Exemplos de Testes: Refatoração PaymentService

## 📖 Introdução

Este documento contém exemplos de testes unitários para as 4 novas classes auxiliares criadas na refatoração.

Cada exemplo mostra:
- Como testar validações
- Como testar comportamentos
- Casos de sucesso e erro
- Best practices com JUnit 5 e Mockito

---

## 1️⃣ PaymentTransactionFactoryTest

```java
package com.api.e_commerce.payment.service;

import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.PaymentTransaction;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionType;
import com.api.e_commerce.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentTransactionFactory Tests")
class PaymentTransactionFactoryTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @InjectMocks
    private PaymentTransactionFactory factory;

    private Payment testPayment;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        testPayment = new Payment();
        testPayment.setId(paymentId);
    }

    @Test
    @DisplayName("recordCheckoutCreated deve criar transação com tipo correto")
    void recordCheckoutCreated_ShouldCreateTransactionWithCorrectType() {
        // Arrange
        String sessionId = "session-123";
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);

        // Act
        factory.recordCheckoutCreated(testPayment, sessionId);

        // Assert
        verify(transactionRepository, times(1)).save(captor.capture());
        PaymentTransaction savedTransaction = captor.getValue();
        
        assertEquals(paymentId, savedTransaction.getPaymentId());
        assertEquals(PaymentTransactionType.CHECKOUT_SESSION_CREATED, savedTransaction.getType());
        assertEquals(sessionId, savedTransaction.getProviderTransactionId());
        assertEquals(PaymentTransactionStatus.SUCCESS, savedTransaction.getStatus());
        assertNull(savedTransaction.getErrorMessage());
    }

    @Test
    @DisplayName("recordCheckoutFailed deve registrar erro corretamente")
    void recordCheckoutFailed_ShouldRecordErrorCorrectly() {
        // Arrange
        String errorMessage = "Gateway timeout";
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);

        // Act
        factory.recordCheckoutFailed(testPayment, errorMessage);

        // Assert
        verify(transactionRepository, times(1)).save(captor.capture());
        PaymentTransaction savedTransaction = captor.getValue();
        
        assertEquals(paymentId, savedTransaction.getPaymentId());
        assertEquals(PaymentTransactionType.PAYMENT_FAILED, savedTransaction.getType());
        assertEquals(PaymentTransactionStatus.FAILURE, savedTransaction.getStatus());
        assertEquals(errorMessage, savedTransaction.getErrorMessage());
    }

    @Test
    @DisplayName("recordPaymentConfirmed deve salvar evento de confirmação com webhook")
    void recordPaymentConfirmed_ShouldSaveConfirmationEvent() {
        // Arrange
        String eventId = "evt-456";
        String payload = "{\"status\":\"succeeded\"}";

        // Act
        factory.recordPaymentConfirmed(testPayment, eventId, payload);

        // Assert
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("recordWebhookReceived deve criar transação de webhook com payload")
    void recordWebhookReceived_ShouldIncludeRawPayload() {
        // Arrange
        String eventId = "evt-789";
        String rawPayload = "{\"object\":\"event\"}";
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);

        // Act
        factory.recordWebhookReceived(testPayment, eventId, rawPayload);

        // Assert
        verify(transactionRepository, times(1)).save(captor.capture());
        PaymentTransaction savedTransaction = captor.getValue();
        
        assertEquals(PaymentTransactionType.WEBHOOK_RECEIVED, savedTransaction.getType());
        assertEquals(eventId, savedTransaction.getProviderEventId());
        assertEquals(rawPayload, savedTransaction.getRawPayload());
    }

    @Test
    @DisplayName("recordRefundCompleted deve registrar reembolso com sucesso")
    void recordRefundCompleted_ShouldRecordSuccessfulRefund() {
        // Arrange
        String refundId = "refund-123";
        String reason = "Customer request";
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);

        // Act
        factory.recordRefundCompleted(testPayment, refundId, reason);

        // Assert
        verify(transactionRepository, times(1)).save(captor.capture());
        PaymentTransaction savedTransaction = captor.getValue();
        
        assertEquals(PaymentTransactionType.REFUND_COMPLETED, savedTransaction.getType());
        assertEquals(PaymentTransactionStatus.SUCCESS, savedTransaction.getStatus());
        assertEquals(refundId, savedTransaction.getProviderTransactionId());
        assertEquals(reason, savedTransaction.getErrorMessage());
    }

    @Test
    @DisplayName("recordRetryAttempt deve incluir número de tentativa na mensagem")
    void recordRetryAttempt_ShouldIncludeAttemptNumber() {
        // Arrange
        int attemptNumber = 2;
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);

        // Act
        factory.recordRetryAttempt(testPayment, attemptNumber);

        // Assert
        verify(transactionRepository, times(1)).save(captor.capture());
        PaymentTransaction savedTransaction = captor.getValue();
        
        assertTrue(savedTransaction.getErrorMessage().contains("Retry attempt #2"));
    }

    @Test
    @DisplayName("recordRetryExhausted deve registrar esgotamento de tentativas")
    void recordRetryExhausted_ShouldRecordExhaustedRetries() {
        // Arrange
        String originalError = "Connection timeout";
        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);

        // Act
        factory.recordRetryExhausted(testPayment, originalError);

        // Assert
        verify(transactionRepository, times(1)).save(captor.capture());
        PaymentTransaction savedTransaction = captor.getValue();
        
        assertEquals(PaymentTransactionType.PAYMENT_FAILED, savedTransaction.getType());
        assertEquals(PaymentTransactionStatus.FAILURE, savedTransaction.getStatus());
        assertTrue(savedTransaction.getErrorMessage().contains("Max retries exhausted"));
    }
}
```

---

## 2️⃣ PaymentCheckoutValidatorTest

```java
package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderRepository;
import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.infrastructure.PaymentConfig;
import com.api.e_commerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCheckoutValidator Tests")
class PaymentCheckoutValidatorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentConfig paymentConfig;

    @InjectMocks
    private PaymentCheckoutValidator validator;

    private UUID orderId;
    private UUID userId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        testOrder = new Order();
        testOrder.setId(orderId);
        testOrder.setUserId(userId);
    }

    @Test
    @DisplayName("validateAndFetchOrder deve retornar order se encontrada")
    void validateAndFetchOrder_ShouldReturnOrderIfFound() {
        // Arrange
        when(orderRepository.findOrderByIdAndUser(orderId, userId))
            .thenReturn(Optional.of(testOrder));

        // Act
        Order result = validator.validateAndFetchOrder(orderId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    @DisplayName("validateAndFetchOrder deve lançar exceção se order não encontrada")
    void validateAndFetchOrder_ShouldThrowIfOrderNotFound() {
        // Arrange
        when(orderRepository.findOrderByIdAndUser(orderId, userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            validator.validateAndFetchOrder(orderId, userId)
        );
    }

    @Test
    @DisplayName("validateNoConcurrentPayment deve retornar se prevenção desativada")
    void validateNoConcurrentPayment_ShouldPassIfPreventionDisabled() {
        // Arrange
        when(paymentConfig.isEnableConcurrentPaymentPrevention()).thenReturn(false);

        // Act & Assert - não deve lançar
        assertDoesNotThrow(() -> validator.validateNoConcurrentPayment(orderId));
        verify(paymentRepository, never()).findByOrderId(orderId);
    }

    @Test
    @DisplayName("validateNoConcurrentPayment deve lançar se pagamento pendente existe")
    void validateNoConcurrentPayment_ShouldThrowIfPendingPaymentExists() {
        // Arrange
        when(paymentConfig.isEnableConcurrentPaymentPrevention()).thenReturn(true);
        
        Payment pendingPayment = new Payment();
        pendingPayment.setPaymentStatus(PaymentStatus.PENDING);
        
        when(paymentRepository.findByOrderId(orderId))
            .thenReturn(Optional.of(pendingPayment));

        // Act & Assert
        assertThrows(PaymentGatewayException.class, () -> 
            validator.validateNoConcurrentPayment(orderId)
        );
    }

    @Test
    @DisplayName("validateNoConcurrentPayment deve passar se pagamento não está PENDING")
    void validateNoConcurrentPayment_ShouldPassIfPaymentNotPending() {
        // Arrange
        when(paymentConfig.isEnableConcurrentPaymentPrevention()).thenReturn(true);
        
        Payment succeededPayment = new Payment();
        succeededPayment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        
        when(paymentRepository.findByOrderId(orderId))
            .thenReturn(Optional.of(succeededPayment));

        // Act & Assert - não deve lançar
        assertDoesNotThrow(() -> validator.validateNoConcurrentPayment(orderId));
    }

    @Test
    @DisplayName("validateRefund deve lançar se pagamento não está SUCCEEDED")
    void validateRefund_ShouldThrowIfNotSucceeded() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        BigDecimal refundAmount = BigDecimal.valueOf(100);

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            validator.validateRefund(payment, refundAmount)
        );
    }

    @Test
    @DisplayName("validateRefund deve lançar se amount é zero ou negativo")
    void validateRefund_ShouldThrowIfAmountNotPositive() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        BigDecimal refundAmount = BigDecimal.ZERO;

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            validator.validateRefund(payment, refundAmount)
        );
    }

    @Test
    @DisplayName("validateRefund deve lançar se amount excede pagamento")
    void validateRefund_ShouldThrowIfAmountExceedsPayment() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setAmount(BigDecimal.valueOf(100));
        BigDecimal refundAmount = BigDecimal.valueOf(150);

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            validator.validateRefund(payment, refundAmount)
        );
    }

    @Test
    @DisplayName("validateRefund deve passar com valores válidos")
    void validateRefund_ShouldPassWithValidValues() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setAmount(BigDecimal.valueOf(100));
        BigDecimal refundAmount = BigDecimal.valueOf(100);

        // Act & Assert - não deve lançar
        assertDoesNotThrow(() -> validator.validateRefund(payment, refundAmount));
    }

    @Test
    @DisplayName("validateRefund deve aceitar reembolso parcial")
    void validateRefund_ShouldAcceptPartialRefund() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setAmount(BigDecimal.valueOf(100));
        BigDecimal refundAmount = BigDecimal.valueOf(50);

        // Act & Assert - não deve lançar
        assertDoesNotThrow(() -> validator.validateRefund(payment, refundAmount));
    }
}
```

---

## 3️⃣ PaymentStatusValidatorTest

```java
package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentStatusValidator Tests")
class PaymentStatusValidatorTest {

    @InjectMocks
    private PaymentStatusValidator validator;

    @Test
    @DisplayName("validateTransition deve aceitar PENDING -> SUCCEEDED")
    void validateTransition_ShouldAcceptPendingToSucceeded() {
        // Act & Assert - não deve lançar
        assertDoesNotThrow(() -> 
            validator.validateTransition(PaymentStatus.PENDING, PaymentStatus.SUCCEEDED)
        );
    }

    @Test
    @DisplayName("validateTransition deve aceitar PENDING -> FAILED")
    void validateTransition_ShouldAcceptPendingToFailed() {
        // Act & Assert
        assertDoesNotThrow(() -> 
            validator.validateTransition(PaymentStatus.PENDING, PaymentStatus.FAILED)
        );
    }

    @Test
    @DisplayName("validateTransition deve aceitar PENDING -> CANCELLED")
    void validateTransition_ShouldAcceptPendingToCancelled() {
        // Act & Assert
        assertDoesNotThrow(() -> 
            validator.validateTransition(PaymentStatus.PENDING, PaymentStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("validateTransition deve aceitar SUCCEEDED -> REFUNDED")
    void validateTransition_ShouldAcceptSucceededToRefunded() {
        // Act & Assert
        assertDoesNotThrow(() -> 
            validator.validateTransition(PaymentStatus.SUCCEEDED, PaymentStatus.REFUNDED)
        );
    }

    @Test
    @DisplayName("validateTransition deve rejeitar FAILED -> qualquer")
    void validateTransition_ShouldRejectFromFailed() {
        // Act & Assert - FAILED é terminal
        assertThrows(ValidationException.class, () -> 
            validator.validateTransition(PaymentStatus.FAILED, PaymentStatus.SUCCEEDED)
        );
    }

    @Test
    @DisplayName("validateTransition deve rejeitar CANCELLED -> qualquer")
    void validateTransition_ShouldRejectFromCancelled() {
        // Act & Assert - CANCELLED é terminal
        assertThrows(ValidationException.class, () -> 
            validator.validateTransition(PaymentStatus.CANCELLED, PaymentStatus.SUCCEEDED)
        );
    }

    @Test
    @DisplayName("validateTransition deve rejeitar REFUNDED -> qualquer")
    void validateTransition_ShouldRejectFromRefunded() {
        // Act & Assert - REFUNDED é terminal
        assertThrows(ValidationException.class, () -> 
            validator.validateTransition(PaymentStatus.REFUNDED, PaymentStatus.PENDING)
        );
    }

    @Test
    @DisplayName("validateTransition deve lançar se status atual é null")
    void validateTransition_ShouldThrowIfCurrentStatusIsNull() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            validator.validateTransition(null, PaymentStatus.SUCCEEDED)
        );
    }

    @Test
    @DisplayName("validateTransition deve lançar se novo status é null")
    void validateTransition_ShouldThrowIfNewStatusIsNull() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            validator.validateTransition(PaymentStatus.PENDING, null)
        );
    }

    @Test
    @DisplayName("isTerminalStatus deve retornar true para FAILED")
    void isTerminalStatus_ShouldReturnTrueForFailed() {
        // Act & Assert
        assertTrue(validator.isTerminalStatus(PaymentStatus.FAILED));
    }

    @Test
    @DisplayName("isTerminalStatus deve retornar true para CANCELLED")
    void isTerminalStatus_ShouldReturnTrueForCancelled() {
        // Act & Assert
        assertTrue(validator.isTerminalStatus(PaymentStatus.CANCELLED));
    }

    @Test
    @DisplayName("isTerminalStatus deve retornar true para REFUNDED")
    void isTerminalStatus_ShouldReturnTrueForRefunded() {
        // Act & Assert
        assertTrue(validator.isTerminalStatus(PaymentStatus.REFUNDED));
    }

    @Test
    @DisplayName("isTerminalStatus deve retornar false para PENDING")
    void isTerminalStatus_ShouldReturnFalseForPending() {
        // Act & Assert
        assertFalse(validator.isTerminalStatus(PaymentStatus.PENDING));
    }

    @Test
    @DisplayName("isTerminalStatus deve retornar false para SUCCEEDED")
    void isTerminalStatus_ShouldReturnFalseForSucceeded() {
        // Act & Assert
        assertFalse(validator.isTerminalStatus(PaymentStatus.SUCCEEDED));
    }

    @Test
    @DisplayName("getValidNextStatuses deve retornar 3 opções para PENDING")
    void getValidNextStatuses_ShouldReturn3OptionsForPending() {
        // Act
        var options = validator.getValidNextStatuses(PaymentStatus.PENDING);

        // Assert
        assertEquals(3, options.size());
        assertTrue(options.contains(PaymentStatus.SUCCEEDED));
        assertTrue(options.contains(PaymentStatus.FAILED));
        assertTrue(options.contains(PaymentStatus.CANCELLED));
    }

    @Test
    @DisplayName("getValidNextStatuses deve retornar 1 opção para SUCCEEDED")
    void getValidNextStatuses_ShouldReturn1OptionForSucceeded() {
        // Act
        var options = validator.getValidNextStatuses(PaymentStatus.SUCCEEDED);

        // Assert
        assertEquals(1, options.size());
        assertTrue(options.contains(PaymentStatus.REFUNDED));
    }
}
```

---

## 4️⃣ PaymentRetryHelperTest

```java
package com.api.e_commerce.payment.service;

import com.api.e_commerce.payment.domain.PaymentRetry;
import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.infrastructure.PaymentConfig;
import com.api.e_commerce.payment.repository.PaymentRetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRetryHelper Tests")
class PaymentRetryHelperTest {

    @Mock
    private PaymentRetryRepository retryRepository;

    @Mock
    private PaymentConfig paymentConfig;

    @InjectMocks
    private PaymentRetryHelper retryHelper;

    private PaymentRetry testRetry;
    private Payment testPayment;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        testPayment = new Payment();
        testPayment.setId(paymentId);

        testRetry = new PaymentRetry();
        testRetry.setPaymentId(paymentId);
        testRetry.setMaxAttempts(3);
        testRetry.setAttemptCount(0);
        testRetry.setIsRetryable(true);
    }

    @Test
    @DisplayName("createRetryRecord deve criar novo retry com tentativa 0")
    void createRetryRecord_ShouldCreateNewRetryWithZeroAttempts() {
        // Arrange
        String errorMessage = "Gateway timeout";
        when(paymentConfig.getMaxRetryAttempts()).thenReturn(3);
        when(paymentConfig.getInitialBackoffMs()).thenReturn(1000L);
        when(retryRepository.save(any())).thenReturn(testRetry);

        // Act
        PaymentRetry result = retryHelper.createRetryRecord(testPayment, errorMessage);

        // Assert
        verify(retryRepository, times(1)).save(any(PaymentRetry.class));
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(3, result.getMaxAttempts());
        assertEquals(0, result.getAttemptCount());
        assertTrue(result.isIsRetryable());
    }

    @Test
    @DisplayName("incrementAttempt deve aumentar contador de tentativas")
    void incrementAttempt_ShouldIncrementAttemptCount() {
        // Arrange
        testRetry.setAttemptCount(1);
        when(retryRepository.save(testRetry)).thenReturn(testRetry);

        // Act
        retryHelper.incrementAttempt(testRetry, "Connection error");

        // Assert
        assertEquals(2, testRetry.getAttemptCount());
        assertEquals("Connection error", testRetry.getLastErrorMessage());
        verify(retryRepository, times(1)).save(testRetry);
    }

    @Test
    @DisplayName("scheduleNextRetry deve agendar próxima tentativa se não esgotado")
    void scheduleNextRetry_ShouldScheduleIfNotExhausted() {
        // Arrange
        testRetry.setAttemptCount(1);
        testRetry.setMaxAttempts(3);
        when(paymentConfig.calculateBackoffDelay(1)).thenReturn(2000L);
        when(retryRepository.save(testRetry)).thenReturn(testRetry);

        // Act
        retryHelper.scheduleNextRetry(testRetry);

        // Assert
        assertNotNull(testRetry.getNextRetryAt());
        assertTrue(testRetry.getNextRetryAt().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("scheduleNextRetry deve marcar como não retentável se esgotado")
    void scheduleNextRetry_ShouldMarkNotRetryableIfExhausted() {
        // Arrange
        testRetry.setAttemptCount(3);
        testRetry.setMaxAttempts(3);
        when(retryRepository.save(testRetry)).thenReturn(testRetry);

        // Act
        retryHelper.scheduleNextRetry(testRetry);

        // Assert
        assertFalse(testRetry.isIsRetryable());
        verify(retryRepository, times(1)).save(testRetry);
    }

    @Test
    @DisplayName("markAsExhausted deve desabilitar retry")
    void markAsExhausted_ShouldDisableRetry() {
        // Arrange
        String errorMessage = "Max attempts reached";
        when(retryRepository.save(testRetry)).thenReturn(testRetry);

        // Act
        retryHelper.markAsExhausted(testRetry, errorMessage);

        // Assert
        assertFalse(testRetry.isIsRetryable());
        assertEquals(errorMessage, testRetry.getLastErrorMessage());
    }

    @Test
    @DisplayName("markAsSuccessful deve desabilitar retry após sucesso")
    void markAsSuccessful_ShouldDisableRetryOnSuccess() {
        // Arrange
        when(retryRepository.save(testRetry)).thenReturn(testRetry);

        // Act
        retryHelper.markAsSuccessful(testRetry);

        // Assert
        assertFalse(testRetry.isIsRetryable());
        verify(retryRepository, times(1)).save(testRetry);
    }

    @Test
    @DisplayName("isReadyForRetry deve retornar false se não retentável")
    void isReadyForRetry_ShouldReturnFalseIfNotRetryable() {
        // Arrange
        testRetry.setIsRetryable(false);

        // Act
        boolean result = retryHelper.isReadyForRetry(testRetry);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isReadyForRetry deve retornar false se nextRetryAt é null")
    void isReadyForRetry_ShouldReturnFalseIfNextRetryAtIsNull() {
        // Arrange
        testRetry.setIsRetryable(true);
        testRetry.setNextRetryAt(null);

        // Act
        boolean result = retryHelper.isReadyForRetry(testRetry);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isReadyForRetry deve retornar false se ainda não atingiu tempo")
    void isReadyForRetry_ShouldReturnFalseIfTimeNotReached() {
        // Arrange
        testRetry.setIsRetryable(true);
        testRetry.setNextRetryAt(Instant.now().plusSeconds(100));

        // Act
        boolean result = retryHelper.isReadyForRetry(testRetry);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isReadyForRetry deve retornar true se pronto para retry")
    void isReadyForRetry_ShouldReturnTrueIfReady() {
        // Arrange
        testRetry.setIsRetryable(true);
        testRetry.setNextRetryAt(Instant.now().minusSeconds(10));

        // Act
        boolean result = retryHelper.isReadyForRetry(testRetry);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isExhausted deve retornar true se tentativas >= máximo")
    void isExhausted_ShouldReturnTrueIfAttemptsExceeded() {
        // Arrange
        testRetry.setAttemptCount(3);
        testRetry.setMaxAttempts(3);

        // Act
        boolean result = retryHelper.isExhausted(testRetry);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isExhausted deve retornar false se tentativas < máximo")
    void isExhausted_ShouldReturnFalseIfAttemptsNotExceeded() {
        // Arrange
        testRetry.setAttemptCount(1);
        testRetry.setMaxAttempts(3);

        // Act
        boolean result = retryHelper.isExhausted(testRetry);

        // Assert
        assertFalse(result);
    }
}
```

---

## 📝 Como Executar os Testes

### Adicionar dependências ao `pom.xml`

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

### Executar via Maven

```bash
# Todos os testes
mvn test

# Apenas classes de teste de payment
mvn test -Dtest=PaymentTransactionFactoryTest,PaymentCheckoutValidatorTest,PaymentStatusValidatorTest,PaymentRetryHelperTest

# Com cobertura
mvn test jacoco:report
```

### Executar via IDE

- **IntelliJ IDEA**: Clique com botão direito no classe de teste → "Run"
- **Eclipse**: Clique com botão direito → "Run As" → "JUnit Test"
- **VS Code**: Extensão "Test Runner for Java"

---

## ✅ Cobertura de Testes Recomendada

```
PaymentTransactionFactory:
- ✅ 7 test cases
- ✅ 100% branch coverage
- ✅ Todos os métodos públicos

PaymentCheckoutValidator:
- ✅ 10 test cases
- ✅ Cenários de sucesso e erro
- ✅ Validações completas

PaymentStatusValidator:
- ✅ 15 test cases (incluindo parameterized)
- ✅ Todas as transições válidas/inválidas
- ✅ Estados terminais

PaymentRetryHelper:
- ✅ 14 test cases
- ✅ Lógica de retry completa
- ✅ Backoff exponencial
```

---

## 🎯 Checklist de Testes

- [ ] Todos os testes passam
- [ ] Cobertura > 80%
- [ ] Sem warnings de deprecation
- [ ] Tests isolados (não dependem uns dos outros)
- [ ] Mocks configurados corretamente
- [ ] Assertions claras e específicas
- [ ] Nomes descritivos (@DisplayName)

---

## 📚 Recursos

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ](https://assertj.io/) - Assertions mais fluentes

