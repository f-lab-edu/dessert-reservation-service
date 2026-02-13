package com.ticketing.common.validation;

import com.ticketing.dto.CoordinateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CoordinateRangeValidator implements ConstraintValidator<ValidCoordinateRange, CoordinateRequest> {

    @Override
    public boolean isValid(CoordinateRequest req, ConstraintValidatorContext context) {
        if (req == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (req.minLatitude() > req.maxLatitude()) {
            context.buildConstraintViolationWithTemplate("minLatitude가 maxLatitude보다 클 수 없습니다.")
                    .addPropertyNode("minLatitude")
                    .addConstraintViolation();
            valid = false;
        }
        if (req.minLongitude() > req.maxLongitude()) {
            context.buildConstraintViolationWithTemplate("minLongitude가 maxLongitude보다 클 수 없습니다.")
                    .addPropertyNode("minLongitude")
                    .addConstraintViolation();
            valid = false;
        }
        return valid;
    }
}
