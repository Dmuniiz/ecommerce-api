package com.api.e_commerce.address;

import com.api.e_commerce.address.client.viacep.ViaCepResponse;
import com.api.e_commerce.config.RestClient;
import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.config.exception.ValidationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final RestClient restClient;

    @Transactional
    public void setAddressAsDefault(UUID userId, UUID addressId) {
        addressRepository.resetDefaultAddress(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ValidationException("Address not found"));

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    public List<Address> listAddressUser(UUID userId) {
        return addressRepository.findAllByUserId(userId);
    }

    public Address create(CreateAddressRequest addressRequest, UUID userId) {
        var client = clientViaCepApi(addressRequest.zipCode());

        Map<String, String> errors = new HashMap<>();

        validateAddressField(errors, "street", addressRequest.street(), client.street());
        validateAddressField(errors, "city", addressRequest.city(), client.city());
        validateAddressField(errors, "state", addressRequest.state(), client.state());

        if (!errors.isEmpty()) {
            throw new ValidationException("Divergent data for CEP:");
        }

        return addressRepository.save(new Address(addressRequest, userId));
    }

    private ViaCepResponse clientViaCepApi(String zipCode) {
        String formatCode = zipCode.replaceAll("\\D", "");

        return restClient.viaCepClient()
                .get()
                .uri("/{zipCode}/json/", formatCode)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ValidationException("Postal code format invalid");
                })
                .body(ViaCepResponse.class);
    }

    private void validateAddressField(Map<String, String> errors, String field, String envoy, String expected) {
        if (expected != null && !expected.isBlank() && !expected.equalsIgnoreCase(envoy)) {
            errors.put(field, "Sent: " + envoy + ". Expected: " + envoy);
        }
    }

}
