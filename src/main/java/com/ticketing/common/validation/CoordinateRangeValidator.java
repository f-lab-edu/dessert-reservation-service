package com.ticketing.common.validation;

import com.ticketing.dto.StoreRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CoordinateRangeValidator implements ConstraintValidator<ValidCoordinateRange, StoreRequest> {

    @Override
    public boolean isValid(StoreRequest req, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (req.minLatitude() > req.maxLatitude()) {
            context.buildConstraintViolationWithTemplate("minLatitude가 maxLatitude보다 클 수 없습니다.")
                    .addPropertyNode("minLatitude")
                    .addConstraintViolation();
            return false;
        }
        if (req.minLongitude() > req.maxLongitude()) {
            context.buildConstraintViolationWithTemplate("minLongitude가 maxLongitude보다 클 수 없습니다.")
                    .addPropertyNode("minLongitude")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
