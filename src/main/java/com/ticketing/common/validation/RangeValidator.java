package com.ticketing.common.validation;

import com.ticketing.dto.StoreRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * StoreRequest의 위도/경도 범위 검증 Validator.
 * min 값이 max 값보다 큰 경우를 검증.
 */
@Component
public class RangeValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return StoreRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StoreRequest req = (StoreRequest) target;

        if (req.minLatitude() > req.maxLatitude()) {
            errors.rejectValue("minLatitude", "range.invalid",
                    "minLatitude가 maxLatitude보다 클 수 없습니다.");
        }
        if (req.minLongitude() > req.maxLongitude()) {
            errors.rejectValue("minLongitude", "range.invalid",
                    "minLongitude가 maxLongitude보다 클 수 없습니다.");
        }
    }
}
