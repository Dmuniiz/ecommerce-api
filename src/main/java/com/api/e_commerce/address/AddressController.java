package com.api.e_commerce.address;

import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.address.dto.AddressesResponse;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressesResponse>> getUserWithAddresses(@AuthenticationPrincipal User userPrincipal){
        List<AddressesResponse> response = addressService.listAddressUser(userPrincipal.getId())
                .stream()
                .map(addr -> AddressesResponse.fromEntity(addr, userPrincipal))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AddressesResponse> createAddress(@RequestBody @Valid CreateAddressRequest addressRequest, @AuthenticationPrincipal User userPrincipal){
        var userId = userPrincipal.getId();
        var addr = addressService.create(addressRequest, userId);

        if(addressRequest.isDefault()){
            addressService.setAddressAsDefault(userId, addr.getId());
        }

       var response = AddressesResponse.fromEntity(addr, userPrincipal);
       return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
