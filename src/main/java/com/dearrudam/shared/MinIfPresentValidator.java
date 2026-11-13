package com.dearrudam.shared;

import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinIfPresentValidator implements ConstraintValidator<MinIfPresent, Optional<Integer>> {

    private long min;

    @Override
    public void initialize(MinIfPresent constraintAnnotation) {
        this.min = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Optional<Integer> value, ConstraintValidatorContext context) {
        return value.isEmpty() || value.get() >= min;
    }
}
