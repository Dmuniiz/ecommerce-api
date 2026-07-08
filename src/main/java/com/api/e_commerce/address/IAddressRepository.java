package com.api.e_commerce.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAddressRepository extends JpaRepository<Address, UUID> {

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Busca um endereço específico validando que pertence ao usuário
     */
    @Query("SELECT a FROM Address a WHERE a.id = :addressId AND a.user.id = :userId")
    Optional<Address> findByIdAndUser_Id(@Param("addressId") UUID addressId, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void resetDefaultAddress(@Param("userId") UUID userId);

    /**
     * Verifica se um endereço com o mesmo CEP e número já existe para o usuário
     */
    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.user.id = :userId AND a.zipCode = :zipCode AND a.number = :number")
    boolean existsByUser_IdAndZipCodeAndNumber(@Param("userId") UUID userId, @Param("zipCode") String zipCode, @Param("number") String number);


    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultByUserId(@Param("userId") UUID userId);


    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

}



