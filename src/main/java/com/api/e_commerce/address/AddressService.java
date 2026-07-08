package com.api.e_commerce.address;

import com.api.e_commerce.address.dto.UpdateAddressRequest;
import com.api.e_commerce.address.dto.CreateAddressRequest;
import com.api.e_commerce.address.exception.AddressNotFoundException;
import com.api.e_commerce.address.exception.UnauthorizedAddressAccessException;
import com.api.e_commerce.address.validator.AddressValidator;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final IAddressRepository addressRepository;
    private final UserService userService;
    private final AddressValidator addressValidator;

    @Transactional(readOnly = true)
    public List<Address> listByUserId(UUID userId) {
        log.debug("Listing addresses for user: {}", userId);
        return addressRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Address getAddressById(UUID addressId, UUID userId) {
        log.debug("Fetching address {} for user {}", addressId, userId);
        return addressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> {
                    log.warn("Address {} not found or unauthorized access by user {}", addressId, userId);
                    return new UnauthorizedAddressAccessException(addressId);
                });
    }

    @Transactional
    public Address create(CreateAddressRequest request, UUID userId) {
        log.info("Creating new address for user: {}", userId);
        
        User user = userService.findUserById(userId);

        addressValidator.validateUniqueness(userId, request);

        // API ViaCep para validar o endereço (se é real)
        addressValidator.validateWithViaCep(request);

        Address address = new Address(request, user);
        Address savedAddress = addressRepository.save(address);
        
        // Se for padrão, remove o padrão anterior
        if (request.isDefault()) {
            setAddressAsDefault(userId, savedAddress.getId());
        }
        
        log.info("Address created successfully: {} for user {}", savedAddress.getId(), userId);
        return savedAddress;
    }

    @Transactional
    public Address updateAddress(UUID userId, UUID addressId, UpdateAddressRequest request) {
        log.info("Updating address {} for user {}", addressId, userId);
        
        Address address = addressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized access to address {} by user {}", addressId, userId);
                    return new UnauthorizedAddressAccessException(addressId);
                });

        address.updateFields(
                request.addressType(),
                request.complement(),
                request.number());
        
        Address updatedAddress = addressRepository.save(address);
        
        log.info("Address {} updated successfully for user {}", addressId, userId);
        return updatedAddress;
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        log.info("Deleting address {} for user {}", addressId, userId);
        
        Address address = addressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized access to delete address {} by user {}", addressId, userId);
                    return new UnauthorizedAddressAccessException(addressId);
                });

        addressRepository.delete(address);
        log.info("Address {} deleted successfully by user {}", addressId, userId);
    }

    @Transactional
    public void setAddressAsDefault(UUID userId, UUID addressId) {
        log.debug("Setting address {} as default for user {}", addressId, userId);
        
        // Remove endereço padrão anterior
        addressRepository.resetDefaultAddress(userId);

        // Define novo endereço padrão
        Address address = addressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        address.setIsDefault(true);
        addressRepository.save(address);
        
        log.info("Address {} set as default for user {}", addressId, userId);
    }


    //=====ADMIN METHODS========

    @Transactional(readOnly = true)
    public Address getDefaultAddress(UUID userId) {
        log.debug("Fetching default address for user {}", userId);
        return addressRepository.findDefaultByUserId(userId)
                .orElseThrow(() -> new AddressNotFoundException("No default address found for user"));
    }

    @Transactional(readOnly = true)
    public long countAddresses(UUID userId) {
        return addressRepository.countByUserId(userId);
    }
}
