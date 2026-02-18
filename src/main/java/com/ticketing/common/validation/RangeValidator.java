package com.ticketing.common.validation;

import com.ticketing.dto.StoreRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class RangeValidator {
    public void validate(StoreRequest req, Errors errors){
        if (req.minLatitude() > req.maxLatitude()) {
            errors.rejectValue("minLatitude", "minLatitude가 maxLatitude보다 클 수 없습니다.");
        }
        if (req.minLongitude() > req.maxLongitude()) {
            errors.rejectValue("minLongitude", "minLongitude가 maxLongitude보다 클 수 없습니다.");
        }
    }
}
