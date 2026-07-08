package com.api.e_commerce.product;

import com.api.e_commerce.config.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Generates standardized SKU (Stock Keeping Unit) for products.
 * Format: PREFIX-SUFFIX
 * Example: PRD-A1B2C3D4
 */
@Slf4j
@Component
public class ProductSkuGenerator {

    private static final int PREFIX_LENGTH = 3;
    private static final int SUFFIX_LENGTH = 8;
    private static final Pattern VALID_SKU_PATTERN = Pattern.compile("^[A-Z0-9]{3}-[A-Z0-9]{8}$");

    /**
     * Generates a unique SKU based on product name and UUID suffix.
     *
     * @param productName the product name to extract prefix from
     * @return a standardized SKU string
     * @throws ValidationException if product name is null or empty
     */
    public String generateSku(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new ValidationException("Product name cannot be null or empty");
        }

        String prefix = extractValidPrefix(productName);
        String suffix = generateUniqueSuffix();
        String sku = prefix + "-" + suffix;

        if (!isValidSku(sku)) {
            log.error("Generated invalid SKU: {}", sku);
            throw new ValidationException("Generated invalid SKU format");
        }

        log.debug("Generated SKU: {} for product: {}", sku, productName);
        return sku;
    }

    /**
     * Extracts a valid 3-character prefix from product name.
     * Removes special characters and ensures uppercase.
     *
     * @param productName the product name
     * @return a valid 3-character prefix
     */
    private String extractValidPrefix(String productName) {
        // Get first 3 alphanumeric characters, convert to uppercase
        String cleaned = productName.replaceAll("[^A-Za-z0-9]", "");

        if (cleaned.isEmpty()) {
            // If no alphanumeric chars, use "PRD" as fallback
            cleaned = "PRD";
        }

        String prefix = cleaned.substring(0, Math.min(cleaned.length(), PREFIX_LENGTH))
                .toUpperCase();

        // Pad with zeros if necessary
        while (prefix.length() < PREFIX_LENGTH) {
            prefix = prefix + "0";
        }

        return prefix;
    }

    /**
     * Generates a unique 8-character suffix using UUID.
     *
     * @return an 8-character alphanumeric suffix
     */
    private String generateUniqueSuffix() {
        return UUID.randomUUID()
                .toString()
                .substring(0, SUFFIX_LENGTH)
                .toUpperCase();
    }

    /**
     * Validates SKU format.
     *
     * @param sku the SKU to validate
     * @return true if SKU matches pattern, false otherwise
     */
    public boolean isValidSku(String sku) {
        if (sku == null) {
            return false;
        }
        return VALID_SKU_PATTERN.matcher(sku).matches();
    }
}

