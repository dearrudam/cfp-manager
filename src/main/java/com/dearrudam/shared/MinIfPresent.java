package com.dearrudam.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that an {@link java.util.Optional} integer, when present, is greater
 * than or equal to {@link #value()}. An empty or absent Optional is always valid —
 * unlike {@code @Min}, which cannot be applied to an Optional container directly.
 */
@Documented
@Constraint(validatedBy = MinIfPresentValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinIfPresent {

    long value();

    String message() default "must be greater than or equal to {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
