package com.api.e_commerce.address.validator;

import com.api.e_commerce.address.IAddressRepository;
import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.address.exception.AddressAlreadyExistsException;
import com.api.e_commerce.address.exception.InvalidAddressException;
import com.api.e_commerce.address.viacep.ViaCepResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddressValidator {

    private final IAddressRepository addressRepository;
    private final RestClient viaCepClient;

    private void validateField(String fieldName, String sent, String expected) {
        if (expected != null && !expected.isBlank()) {
            if (!expected.equalsIgnoreCase(sent)) {
                log.warn("Field divergence detected for '{}': sent={}, expected={}",
                        fieldName, sent, expected);
                throw new InvalidAddressException(fieldName, sent, expected);
            }
        }
    }

    public void validateUniqueness(UUID userId, CreateAddressRequest request) {
        if (addressRepository.existsByUser_IdAndZipCodeAndNumber(
                userId, request.zipCode(), request.number())) {
            log.warn("Address already exists for user {} with zipCode {} and number {}",
                    userId, request.zipCode(), request.number());
            throw new AddressAlreadyExistsException(
                    request.zipCode(),
                    request.number()
            );
        }
    }

    public void validateWithViaCep(CreateAddressRequest request) {
        try {
            log.debug("Validating address with ViaCep for zipCode: {}", request.zipCode());

            ViaCepResponse viaCepData = fetchViaCepData(request.zipCode());

            // Se houver erro na resposta do ViaCep
            if (viaCepData.error() != null && viaCepData.error()) {
                log.error("Invalid postal code from ViaCep: {}", request.zipCode());
                throw new InvalidAddressException("Invalid postal code: " + request.zipCode());
            }

            // Valida cada campo
            validateField("street", request.street(), viaCepData.street());
            validateField("city", request.city(), viaCepData.city());
            validateField("neighborhood", request.neighborhood(), viaCepData.neighborhood());
            validateField("state", request.state(), viaCepData.state());

            log.debug("Address validation succeeded for zipCode: {}", request.zipCode());
        } catch (InvalidAddressException e) {
            throw e;
        } catch (Exception e) {
            log.error("ViaCep validation failed for zipCode: {}", request.zipCode(), e);
            throw new InvalidAddressException("Unable to validate postal code with ViaCep: " + e.getMessage());
        }
    }

    private ViaCepResponse fetchViaCepData(String zipCode) {
        String formatCode = zipCode.replaceAll("\\D", "");

        return viaCepClient
                .get()
                .uri("/{zipCode}/json/", formatCode)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error("ViaCep returned 4xx error for zipCode: {}", zipCode);
                    throw new InvalidAddressException("Invalid postal code format: " + zipCode);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("ViaCep returned 5xx error for zipCode: {}", zipCode);
                    throw new InvalidAddressException("ViaCep service unavailable");
                })
                .body(ViaCepResponse.class);
    }
}

