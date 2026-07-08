package com.api.e_commerce.order.validator;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.cart.Cart;
import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.order.dto.CreateOrderRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderCreationValidator {

    public void validate(CreateOrderRequest request, Cart cart, List<Address> addresses) {
        validateCart(cart);
        validateShippingAddress(request.shippingAddressId(), addresses);
        validateBillingAddress(request.billingAddressId(), addresses);
        validateProducts(cart);
    }

    private void validateCart(Cart cart) {
        if (cart == null) {
            throw new ValidationException("Cart not found");
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new ValidationException("Cart is empty. Cannot create order from empty cart");
        }

        if (cart.getTotalAmount() == null || cart.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Cart total amount must be greater than zero");
        }
    }

    private void validateShippingAddress(UUID shippingAddressId, List<Address> addresses) {
        if (shippingAddressId == null) {
            throw new ValidationException("Shipping address ID is required");
        }

        Address shipping = addresses.stream()
                .filter(addr -> addr.getId().equals(shippingAddressId)
                        && addr.getAddressType() != null
                        && addr.getAddressType().contains(AddressType.SHIPPING))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Shipping address not found or invalid"));

        validateAddressData(shipping, "Shipping");
    }

    private void validateBillingAddress(UUID billingAddressId, List<Address> addresses) {
        if (billingAddressId == null) {
            throw new ValidationException("Billing address ID is required");
        }

        Address billing = addresses.stream()
                .filter(addr -> addr.getId().equals(billingAddressId)
                        && addr.getAddressType() != null
                        && addr.getAddressType().contains(AddressType.BILLING))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Billing address not found or invalid"));

        validateAddressData(billing, "Billing");
    }

    private void validateAddressData(Address address, String addressType) {
        if (address.getStreet() == null || address.getStreet().isBlank()) {
            throw new ValidationException(addressType + " address street is required");
        }
        if (address.getCity() == null || address.getCity().isBlank()) {
            throw new ValidationException(addressType + " address city is required");
        }
        if (address.getZipCode() == null || address.getZipCode().isBlank()) {
            throw new ValidationException(addressType + " address zip code is required");
        }
    }

    private void validateProducts(Cart cart) {
        cart.getCartItems().forEach(cartItem -> {
            if (cartItem.getProduct() == null) {
                throw new ValidationException("Cart contains invalid product");
            }
            if (cartItem.getQuantity() <= 0) {
                throw new ValidationException("Cart item quantity must be positive");
            }
        });
    }
}

