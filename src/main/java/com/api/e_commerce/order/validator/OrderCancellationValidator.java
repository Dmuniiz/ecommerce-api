package com.api.e_commerce.order.validator;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.order.OrderStatus;
import com.api.e_commerce.order.exception.InvalidOrderStateException;
import com.api.e_commerce.order.exception.UnauthorizedOrderAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validator for order cancellation requests
 * Ensures business rules are met before cancelling an order
 */
@Component
public class OrderCancellationValidator {


    public void validate(Order order, UUID requestingUserId) {
        validateOwnership(order, requestingUserId);
        validateState(order);
    }

    private void validateOwnership(Order order, UUID requestingUserId) {
        if (!order.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedOrderAccessException(order.getId(), requestingUserId);
        }
    }

    private void validateState(Order order) {
        switch (order.getStatus()) {
            case SHIPPED, DELIVERED, REFUNDED -> {
                throw new InvalidOrderStateException(
                        order.getStatus(),
                        "cancel"
                );
            }
            case CANCELLED -> {
                throw new InvalidOrderStateException(
                        "Order is already cancelled"
                );
            }
            case CREATED, PENDING_PAYMENT, PAID, PAYMENT_FAILED -> {
                // Valid states for cancellation
            }
        }
    }
}

