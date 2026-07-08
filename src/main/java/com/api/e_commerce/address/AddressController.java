package com.api.e_commerce.address;

import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.address.dto.AddressResponse;
import com.api.e_commerce.address.dto.UpdateAddressRequest;
import com.api.e_commerce.address.mapper.AddressMapper;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;
    private final AddressMapper addressMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressResponse>> listAddresses(
            @AuthenticationPrincipal User user) {

        log.info("User {} requested to list addresses", user.getId());
        
        List<Address> addresses = addressService.listByUserId(user.getId());
        List<AddressResponse> response = addressMapper.toDtoList(addresses);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> getAddressById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user) {
        log.info("User {} requested to fetch address {}", user.getId(), id);
        
        Address address = addressService.getAddressById(id, user.getId());
        AddressResponse response = addressMapper.toDto(address);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            @AuthenticationPrincipal User user) {

        log.info("User {} creating new address", user.getId());
        
        Address address = addressService.create(request, user.getId());
        AddressResponse response = addressMapper.toDto(address);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateAddressRequest request,
            @AuthenticationPrincipal User user) {
        log.info("User {} updating address {}", user.getId(), id);
        
        Address address = addressService.updateAddress(user.getId(), id, request);
        AddressResponse response = addressMapper.toDto(address);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user) {
        log.info("User {} deleting address {}", user.getId(), id);
        
        addressService.deleteAddress(user.getId(), id);

        //204
        return ResponseEntity.noContent().build();
    }

}
