package com.api.e_commerce.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUserId(UUID userId);

    @Modifying(clearAutomatically = true) //dml or ddl
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId AND a.isDefault = true")
    void resetDefaultAddress(UUID userId);

    Optional<Address> findByUserIdAndZipCodeAndNumber(UUID userId, String zipCode, String number);

    @Query("SELECT a FROM Address a WHERE a.id = :addressId AND a.userId = :userId ")
    Optional<Address> findByIdAndUserId(UUID addressId, @Param("userId") UUID userId);


    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.userId = :userId AND a.zipCode = :zipCode AND a.number = :number")
    boolean existsByUserIdAndZipCodeAndNumber(@Param("userId") UUID userId, @Param("zipCode") String zipCode, @Param("number") String number);

}



