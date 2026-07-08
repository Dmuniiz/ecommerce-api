package com.api.e_commerce.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.userId = :userId")
    Optional<Order> findOrderByIdAndUser(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Order> findByPaymentId(UUID paymentId);

    /**
     * Find orders for a user with specific status
     * Orders by creation date descending
     */
    @Query("""
        SELECT o FROM Order o
        WHERE o.userId = :userId AND o.status = :status
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    /**
     * Find order by ID with items eagerly loaded to prevent N+1
     * Useful for order details endpoint
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.items
        WHERE o.id = :id AND o.userId = :userId
    """)
    Optional<Order> findByIdAndUserWithItems(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );

    /**
     * Find all orders for user with items eagerly loaded
     * Prevents N+1 problem when listing orders
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.items
        WHERE o.userId = :userId
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findAllByUserIdWithItems(
            @Param("userId") UUID userId,
            Pageable pageable
    );
}


