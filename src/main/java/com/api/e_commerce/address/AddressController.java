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
    public ResponseEntity<List<AddressesResponse>> getAddressesWithUser(@AuthenticationPrincipal User user) {
        List<AddressesResponse> response = addressService.listAddressesByUserId(user.getId())
                .stream()
                .map(addr -> AddressesResponse.fromEntity(addr, user))
                .toList();
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<AddressesResponse> register(@RequestBody @Valid CreateAddressRequest request, @AuthenticationPrincipal User user) {
        var addr = addressService.create(request, user.getId());

        if (request.isDefault()) {
            addressService.setAddressAsDefault(user.getId(), addr.getId());
        }

        var response = AddressesResponse.fromEntity(addr, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
