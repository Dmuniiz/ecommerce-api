package com.api.e_commerce.product;

import com.api.e_commerce.config.exception.ValidationException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToProductStatusConverter implements Converter<String, ProductStatus> {
    @Override
    public ProductStatus convert(String source) {

        if (source.isBlank()) {
            return null;
        }

        String formatted = source.trim().toUpperCase().replace(" ", "_");

        try {
            return ProductStatus.valueOf(formatted);
        }catch (IllegalArgumentException e) {
            throw new ValidationException("Product status not supported" + source);
        }
    }
}
