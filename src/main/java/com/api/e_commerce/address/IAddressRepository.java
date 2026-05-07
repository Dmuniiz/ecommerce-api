package com.api.e_commerce.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUserId(UUID userId);

    @Modifying(clearAutomatically = true) //dml or ddl
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId AND a.isDefault = true")
    void resetDefaultAddress(UUID userId);

}



