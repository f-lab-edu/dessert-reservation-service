package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.dto.StoreRes;
import com.ticketing.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreController extends BaseController {

    private final StoreService storeService;

    /**
     * 지도 화면에서 사용자의 화면 범위(위도/경도) 내에 있는 스토어 목록 조회.
     * 각 스토어의 위치 정보와 잔여 디저트 수량을 함께 반환.
     */
    @GetMapping("/stores")
    public ResponseEntity<List<StoreRes>> getStoreList(
            @RequestParam double minLatitude,
            @RequestParam double maxLatitude,
            @RequestParam double minLongitude,
            @RequestParam double maxLongitude
    ) {
        return ResponseEntity.ok(storeService.getStoreList(minLatitude, maxLatitude, minLongitude, maxLongitude));
    }
}
