package com.dearrudam.shared;

import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresent, Optional<String>> {

    @Override
    public boolean isValid(Optional<String> value, ConstraintValidatorContext context) {
        return value.isEmpty() || !value.get().isBlank();
    }
}
