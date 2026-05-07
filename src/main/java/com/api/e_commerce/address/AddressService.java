package com.api.e_commerce.address;

import com.api.e_commerce.address.viacep.ViaCepResponse;
import com.api.e_commerce.config.client.RestClient;
import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.config.exception.ValidationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final IAddressRepository addressRepository;
    private final RestClient restClient;

    @Transactional
    public void setAddressAsDefault(UUID userId, UUID addressId) {
        addressRepository.resetDefaultAddress(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ValidationException("Address not found"));

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<Address> listAddressesByUserId(UUID userId) {
        return addressRepository.findAllByUserId(userId);
    }

    public Address create(CreateAddressRequest data, UUID userId) {
        var client = clientViaCepApi(data.zipCode());

        Map<String, String> errors = new HashMap<>();

        validateAddressField(errors, "street", data.street(), client.street());
        validateAddressField(errors, "city", data.city(), client.city());
        validateAddressField(errors, "state", data.state(), client.state());

        if (!errors.isEmpty()) {
            throw new ValidationException("Divergent data for POSTAL CODE:");
        }

        return addressRepository.save(new Address(data, userId));
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
