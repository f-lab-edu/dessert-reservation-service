package com.ticketing.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CoordinateRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCoordinateRange {
    String message() default "잘못된 좌표 범위입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
