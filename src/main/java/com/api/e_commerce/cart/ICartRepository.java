package com.api.e_commerce.cart;


import com.api.e_commerce.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ICartRepository extends JpaRepository<Cart, UUID> {
    void deleteByUpdatedAtBefore(Instant threshold);

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(UUID userId);

    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.product " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserWithItems(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.product " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserWithItemsForUpdate(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId")
    int deleteAllCartItemsFromUser(@Param("userId") UUID userId);

    Optional<Cart> findCartByIdAndUserId(UUID cartId, UUID userId);

    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.product " +
            "WHERE c.id = :cartId AND c.user.id = :userId")
    Optional<Cart> findCartByIdAndUserIdWithItems(@Param("cartId") UUID cartId, @Param("userId") UUID userId);
}
