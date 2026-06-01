package com.api.e_commerce.cart;


import com.api.e_commerce.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ICartRepository extends JpaRepository<Cart, UUID> {
    void deleteByUpdatedAtBefore(LocalDateTime threshold);

    Optional<Cart> findByUser(User user);

    @Query("SELECT c FROM Cart c " +
            "JOIN FETCH c.cartItems ci " +
            "JOIN FETCH ci.product " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserWithItems(@Param("userId") UUID userId);


    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product.id = :productId AND ci.cart.user.id = :userId")
    int deleteCartItemByIdFromUser(@Param("productId") UUID productId,  @Param("userId") UUID userId);

    Optional<Cart> findCartByIdAndUserId(UUID cartId, UUID userId);
}
