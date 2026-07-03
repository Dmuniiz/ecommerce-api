package com.api.e_commerce.payment.service;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.payment.domain.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Validador de transições de estado de pagamento
 * Implementa uma máquina de estados para garantir transições válidas
 *
 * ANTES: validateStatusTransition() com switch complexo e duplicado
 * DEPOIS: Map estático com transições válidas definidas em um lugar
 *
 * Melhoria: Mais fácil de manter, visualizar e adicionar novas transições
 *
 * Estados:
 * - PENDING:   Aguardando processamento pelo gateway
 * - SUCCEEDED: Pagamento confirmado com sucesso
 * - FAILED:    Pagamento falhou (terminal, requer retry)
 * - CANCELLED: Pagamento cancelado pelo usuário (terminal)
 * - REFUNDED:  Pagamento reembolsado (terminal)
 *
 * Transições válidas:
 * PENDING   → SUCCEEDED, FAILED, CANCELLED
 * SUCCEEDED → REFUNDED
 * FAILED    → (nenhuma - use PaymentRetry para retry)
 * CANCELLED → (nenhuma - terminal)
 * REFUNDED  → (nenhuma - terminal)
 */
@Slf4j
@Component
public class PaymentStatusValidator {

    /**
     * Mapa com transições válidas de estado
     * Mais legível que switch/if statements complexos
     */
    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.ofEntries(
        Map.entry(PaymentStatus.PENDING, Set.of(
            PaymentStatus.SUCCEEDED,
            PaymentStatus.FAILED,
            PaymentStatus.CANCELLED
        )),
        Map.entry(PaymentStatus.SUCCEEDED, Set.of(
            PaymentStatus.REFUNDED
        )),
        // Estados terminais - nenhuma transição válida
        Map.entry(PaymentStatus.FAILED, Set.of()),
        Map.entry(PaymentStatus.CANCELLED, Set.of()),
        Map.entry(PaymentStatus.REFUNDED, Set.of())
    );

    /**
     * Valida se uma transição de estado é permitida
     * Lança exceção se a transição não é válida
     *
     * @param currentStatus Status atual do pagamento
     * @param newStatus     Status desejado
     * @throws ValidationException se a transição não é válida
     */
    public void validateTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == null) {
            throw new ValidationException("Current payment status cannot be null");
        }
        if (newStatus == null) {
            throw new ValidationException("New payment status cannot be null");
        }

        var validNextStatuses = VALID_TRANSITIONS.get(currentStatus);

        if (validNextStatuses == null) {
            throw new ValidationException(
                "Unknown payment status: " + currentStatus
            );
        }

        if (!validNextStatuses.contains(newStatus)) {
            throw new ValidationException(
                String.format("Invalid payment status transition: %s → %s. " +
                    "Valid transitions from %s: %s",
                    currentStatus, newStatus, currentStatus, validNextStatuses.isEmpty() ? "NONE (terminal)" : validNextStatuses)
            );
        }

        log.debug("Valid transition: {} → {}", currentStatus, newStatus);
    }

    /**
     * Verifica se um status é terminal (sem transições válidas)
     * Útil para saber se um pagamento ainda pode ser processado
     */
    public boolean isTerminalStatus(PaymentStatus status) {
        var validTransitions = VALID_TRANSITIONS.get(status);
        return validTransitions != null && validTransitions.isEmpty();
    }

    /**
     * Retorna os status próximos válidos para um status dado
     * Útil para logging e debugging
     */
    public Set<PaymentStatus> getValidNextStatuses(PaymentStatus status) {
        return VALID_TRANSITIONS.getOrDefault(status, Set.of());
    }
}

