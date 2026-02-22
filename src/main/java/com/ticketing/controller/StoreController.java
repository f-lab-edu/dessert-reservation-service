package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.common.validation.RangeValidator;
import com.ticketing.dto.StoreRequest;
import com.ticketing.dto.DessertRes;
import com.ticketing.dto.StoreRes;
import com.ticketing.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreController extends BaseController {

    private final StoreService storeService;
    private final RangeValidator rangeValidator;

    /**
     * StoreRequest에 대한 커스텀 Validator 등록.
     * @InitBinder를 사용하여 자동으로 검증 수행.
     */
    @InitBinder("storeRequest")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(rangeValidator);
    }

    /**
     * 지도 화면에서 사용자의 화면 범위(위도/경도) 내에 있는 스토어 목록 조회.
     * 각 스토어의 위치 정보와 잔여 디저트 수량을 함께 반환.
     * 위도(-90~90), 경도(-180~180) 범위 및 min/max 역전 여부를 검증하며, 유효하지 않으면 400 반환.
     */
    @GetMapping("/stores")
    public ResponseEntity<List<StoreRes>> getStoreList(@Valid @ModelAttribute StoreRequest req) {
        return ResponseEntity.ok(storeService.getStoreList(
                req.minLatitude(), req.maxLatitude(), req.minLongitude(), req.maxLongitude()
        ));
    }

    /**
     * storeId로 스토어에 등록된 디저트 목록 조회.
     * 존재하지 않는 storeId인 경우 빈 배열 반환.
     */
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<List<DessertRes>> getStoreDesserts(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getStoreDesserts(storeId));
    }
}
