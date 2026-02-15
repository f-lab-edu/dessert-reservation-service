package com.ticketing.common.validation;

import com.ticketing.common.validation.RangeValidator;
import com.ticketing.dto.StoreRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;

class RangeValidatorTest {

    private final RangeValidator rangeValidator = new RangeValidator();

    private Errors createErrors(StoreRequest req) {
        return new BeanPropertyBindingResult(req, "storeRequest");
    }

    @Test
    void 정상_범위_에러없음() {
        StoreRequest req = new StoreRequest(37.0, 38.0, 127.0, 128.0);
        Errors errors = createErrors(req);

        rangeValidator.validate(req, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void minLatitude가_maxLatitude보다_크면_에러() {
        StoreRequest req = new StoreRequest(38.0, 37.0, 127.0, 128.0);
        Errors errors = createErrors(req);

        rangeValidator.validate(req, errors);

        assertTrue(errors.hasFieldErrors("minLatitude"));
    }

    @Test
    void minLongitude가_maxLongitude보다_크면_에러() {
        StoreRequest req = new StoreRequest(37.0, 38.0, 128.0, 127.0);
        Errors errors = createErrors(req);

        rangeValidator.validate(req, errors);

        assertTrue(errors.hasFieldErrors("minLongitude"));
    }

    @Test
    void 위도_경도_모두_역전되면_에러_두개() {
        StoreRequest req = new StoreRequest(38.0, 37.0, 128.0, 127.0);
        Errors errors = createErrors(req);

        rangeValidator.validate(req, errors);

        assertEquals(2, errors.getErrorCount());
        assertTrue(errors.hasFieldErrors("minLatitude"));
        assertTrue(errors.hasFieldErrors("minLongitude"));
    }
}
