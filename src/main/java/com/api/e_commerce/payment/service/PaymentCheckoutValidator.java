package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.PaymentGatewayException;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderRepository;
import com.api.e_commerce.payment.domain.Payment;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import com.api.e_commerce.payment.infrastructure.PaymentConfig;
import com.api.e_commerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Validator for checkout operations
 * Centralizes all checkout validations in a single place
 *
 * BEFORE: Validations scattered throughout createCheckoutSession()
 * AFTER: A specific method for each validation
 *
 * Improvement: More readable(legível) and reusable code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCheckoutValidator {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentConfig paymentConfig;

    /**
     * @throws ValidationException se order não existe ou não pertence ao usuário
     */
    public Order validateAndFetchOrder(UUID orderId, UUID userId) {
        return orderRepository.findOrderByIdAndUser(orderId, userId)
            .orElseThrow(() -> {
                log.warn("Order not found or doesn't belong to user: order={}, user={}", orderId, userId);
                return new ValidationException("Order not found");
            });
    }

    /**
     * Previne race condition de múltiplas tentativas de checkout simultâneas
     * @throws PaymentGatewayException se já existe pagamento pendente
     */
    public void validateNoConcurrentPayment(UUID orderId) {
        if (!paymentConfig.isEnableConcurrentPaymentPrevention()) {
            return;
        }

        paymentRepository.findByOrderId(orderId)
            .filter(p -> PaymentStatus.PENDING.equals(p.getPaymentStatus()))
            .ifPresent(p -> {
                log.warn("Concurrent payment attempt detected for order {}", orderId);
                throw new PaymentGatewayException("Payment already in progress for this order");
            });
    }

    /**
     * Valida refund (comum a todas as operações de refund)
     *
     * @throws ValidationException se refund não é válido
     */
    public void validateRefund(Payment payment, BigDecimal refundAmount) {
        // Validar status do pagamento
        if (!PaymentStatus.SUCCEEDED.equals(payment.getPaymentStatus())) {
            throw new ValidationException(
                "Only succeeded payments can be refunded. Current status: " + payment.getPaymentStatus()
            );
        }

        // Validar amount positivo
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be greater than zero");
        }

        // Validar refund não excede o pagamento
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new ValidationException(
                "Refund amount cannot exceed paid amount: " +
                "refund=" + refundAmount + ", paid=" + payment.getAmount()
            );
        }

        log.debug("Refund validation passed for payment {}: amount={}", payment.getId(), refundAmount);
    }

    public void validatePaymentExists(Payment payment) {
        if (payment == null) {
            throw new ValidationException("Payment not found or invalid");
        }
    }

    /**
     * Valida que order está em estado correto para checkout
     * @throws ValidationException se order não está em estado válido
     */
    public void validateOrderCheckoutable(Order order) {
        // Aqui você pode adicionar lógica adicional se necessário
        // Por exemplo: verificar se order já foi pago, etc
        if (order == null) {
            throw new ValidationException("Order is invalid");
        }
    }
}

