package com.api.e_commerce.order;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.AddressService;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.cart.Cart;
import com.api.e_commerce.cart.CartService;
import com.api.e_commerce.cart.cartItem.CartItem;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.dto.OrderResponse;
import com.api.e_commerce.order.dto.OrderTrackingResponse;
import com.api.e_commerce.order.exception.InvalidOrderStateException;
import com.api.e_commerce.order.exception.OrderNotFoundException;
import com.api.e_commerce.order.mapper.OrderMapper;
import com.api.e_commerce.order.orderItem.OrderItem;
import com.api.e_commerce.order.validator.OrderCancellationValidator;
import com.api.e_commerce.order.validator.OrderCreationValidator;
import com.api.e_commerce.payment.domain.enums.PaymentTransactionStatus;
import com.api.e_commerce.payment.service.PaymentService;
import com.api.e_commerce.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final CartService cartService;
    private final ProductService productService;
    private final AddressService addressService;
    private final PaymentService paymentService;

    private final OrderMapper orderMapper;

    private final OrderCreationValidator orderCreationValidator;
    private final OrderCancellationValidator orderCancellationValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(UUID userId, UUID cartId, UUID shipId, UUID billId, String idempotencyKey) {
        log.info("Creating order for user {} from cart {}", userId, cartId);

        try {
            // Fetch and validate cart
            Cart cart = cartService.findByIdAndUserId(cartId, userId);
            List<Address> addresses = addressService.listByUserId(userId);

            // Validate order creation prerequisites
            orderCreationValidator.
                    validate(new com.api.e_commerce.order.dto.CreateOrderRequest(cartId, shipId, billId), cart, addresses);

            // Get validated addresses
            Address shipping = validateAddress(addresses, shipId, AddressType.SHIPPING);
            Address billing = validateAddress(addresses, billId, AddressType.BILLING);

            Order order = buildOrder(userId, cart, shipping, billing);

            // Persist order
            Order saved = orderRepository.save(order);
            log.info("Order {} created successfully for user {}", saved.getId(), userId);

            cartService.clearCartFromCreateOrder(cart);

            // Publish event for async processing (emails, notifications, etc)
            eventPublisher.publishEvent(new OrderCreatedEvent(this, saved.getId(), userId));

            return saved;

        } catch (Exception e) {
            log.error("Failed to create order for user {} from cart {}: {}", userId, cartId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * List all orders for a user with pagination
     * Uses fetch join to prevent N+1 problem
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> listUserOrders(UUID userId, int page, int size) {
        log.debug("Listing orders for user {} - page: {}, size: {}", userId, page, size);

        try {
            PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            var ordersPage = orderRepository.findAllByUserIdWithItems(userId, pageable);
            return ordersPage.map(orderMapper::toOrderResponse);

        } catch (Exception e) {
            log.error("Failed to list orders for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * List orders by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrdersByStatus(UUID userId, OrderStatus status, int page, int size) {
        log.debug("Listing orders for user {} with status {} - page: {}, size: {}", userId, status, page, size);

        try {
            PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            var ordersPage = orderRepository.findByUserIdAndStatus(userId, status, pageable);
            return ordersPage.map(orderMapper::toOrderResponse);

        } catch (Exception e) {
            log.error("Failed to list orders for user {} with status {}: {}", userId, status, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get order details by ID with authorization check
     */
    @Transactional(readOnly = true)
    public Order getOrderDetails(UUID orderId, UUID userId) {
        log.debug("Fetching order {} for user {}", orderId, userId);

        return orderRepository.findByIdAndUserWithItems(orderId, userId)
                .orElseThrow(() -> {
                    log.warn("Order {} not found for user {}", orderId, userId);
                    return new OrderNotFoundException(orderId);
                });
    }

    /**
     * Get order tracking information
     */
    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(UUID orderId, UUID userId) {
        log.debug("Fetching tracking for order {} - user {}", orderId, userId);

        Order order = getOrderDetails(orderId, userId);
        return orderMapper.toTrackingResponse(order);
    }

    /**
     * Cancel an order with reason
     * Only cancellable in certain states (CREATED, PENDING_PAYMENT, PAID, PAYMENT_FAILED)
     */
    @Transactional
    public Order cancelOrder(UUID orderId, UUID userId, String reason) {
        log.info("Cancelling order {} for user {} - reason: {}", orderId, userId, reason);

        try {
            Order order = getOrderDetails(orderId, userId);

            // Validate cancellation is allowed
            orderCancellationValidator.validate(order, userId);

            // Cancel order
            order.cancel();
            order.setUpdatedAt(Instant.now());

            Order cancelled = orderRepository.save(order);
            log.info("Order {} cancelled successfully", orderId);

            // Publish cancellation event
            eventPublisher.publishEvent(new OrderCancelledEvent(this, orderId, userId, reason));

            return cancelled;

        } catch (OptimisticLockingFailureException e) {
            log.warn("Order {} is being updated concurrently, retry operation", orderId);
            throw new ValidationException("Order is being processed, please try again");
        } catch (Exception e) {
            log.error("Failed to cancel order {} for user {}: {}", orderId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void confirmPayment(UUID orderId, String eventId, String rawPayload) {
        log.info("Confirming payment for order {} - event: {}", orderId, eventId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.warn("Order {} not found during payment confirmation", orderId);
                        return new OrderNotFoundException(orderId);
                    });

            // Skip if already paid
            if (order.getStatus() == OrderStatus.PAID) {
                log.debug("Order {} already paid, skipping payment confirmation", orderId);
                return;
            }

            // Validate order can be marked as paid
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT &&
                    order.getStatus() != OrderStatus.CREATED &&
                    order.getStatus() != OrderStatus.PAYMENT_FAILED) {
                log.warn("Order {} in invalid state for payment confirmation: {}", orderId, order.getStatus());
                throw new InvalidOrderStateException(order.getStatus(), "confirm payment");
            }

            // Update payment records (will validate payment and clear retries)
            paymentService.updatePaymentStatus(order, PaymentTransactionStatus.SUCCESS, eventId, rawPayload);

            decreaseOrderItemsStock(order);

            // Mark order as paid with timestamp
            order.markAsPaid();
            orderRepository.save(order); //Order saved

            log.info("Payment confirmed for order {}", orderId);

            // Publish payment confirmed event
            eventPublisher.publishEvent(
                    new OrderPaidEvent(this, orderId, order.getUserId())
            );

        } catch (OptimisticLockingFailureException e) {
            log.warn("Order {} is being updated concurrently during payment confirmation", orderId);
            throw new ValidationException("Order is being processed, please try again");

        } catch (Exception e) {
            log.error("Failed to confirm payment for order {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Build order entity from cart and addresses
     */
    private Order buildOrder(UUID userId, Cart cart, Address shipping, Address billing) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(cart.getTotalAmount());
        order.setCurrency("BRL"); // Default currency
        order.setShippingAddress(new OrderAddress(shipping));
        order.setBillingAddress(new OrderAddress(billing));
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> items = cart.getCartItems().stream()
                .map(cartItem -> putOrderItemFromCart(order, cartItem))
                .toList();

        order.setItems(items);
        return order;
    }

    private OrderItem putOrderItemFromCart(Order order, CartItem cartItem) {
        OrderItem item = new OrderItem();
        item.setItem(cartItem);
        item.setOrder(order);
        return item;
    }

    private void decreaseOrderItemsStock(Order order) {
        log.debug("Decreasing stock for {} items in order {}", order.getItems().size(), order.getId());

        for (OrderItem item : order.getItems()) {
            try {
                productService.decreaseStock(item.getProduct().getId(), item.getQuantity());
                log.debug("Decreased stock for product {} by {}", item.getProduct().getId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to decrease stock for product {}: {}", item.getProduct().getId(), e.getMessage(), e);
                throw new ValidationException("Failed to decrease stock: " + e.getMessage());
            }
        }
    }

    private Address validateAddress(
            List<Address> addresses,
            UUID addressId,
            AddressType type
    ) {
        return addresses.stream()
                .filter(address ->
                        address.getId().equals(addressId)
                                && address.getAddressType() != null
                                && address.getAddressType().contains(type)
                )
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Address {} not found or invalid for type {}", addressId, type);
                    return new ValidationException(
                            "Address %s not found or invalid for %s".formatted(addressId, type)
                    );
                });
    }

    // ==================== Event Classes ====================

    public static class OrderCreatedEvent extends org.springframework.context.ApplicationEvent {
        private final UUID orderId;
        private final UUID userId;

        public OrderCreatedEvent(Object source, UUID orderId, UUID userId) {
            super(source);
            this.orderId = orderId;
            this.userId = userId;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public UUID getUserId() {
            return userId;
        }
    }


    public static class OrderPaidEvent extends org.springframework.context.ApplicationEvent {
        private final UUID orderId;
        private final UUID userId;

        public OrderPaidEvent(Object source, UUID orderId, UUID userId) {
            super(source);
            this.orderId = orderId;
            this.userId = userId;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public UUID getUserId() {
            return userId;
        }
    }

    /**
     * Event published when order is cancelled
     */
    public static class OrderCancelledEvent extends org.springframework.context.ApplicationEvent {
        private final UUID orderId;
        private final UUID userId;
        private final String reason;

        public OrderCancelledEvent(Object source, UUID orderId, UUID userId, String reason) {
            super(source);
            this.orderId = orderId;
            this.userId = userId;
            this.reason = reason;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getReason() {
            return reason;
        }
    }
}
