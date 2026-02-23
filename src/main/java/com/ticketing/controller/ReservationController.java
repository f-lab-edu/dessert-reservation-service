package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;
import com.ticketing.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController extends BaseController {

    private final ReservationService reservationService;

    /**
     * 로그인한 사용자의 디저트 예약 요청 처리.
     * SecurityConfig에서 인증된 사용자만 접근 가능하도록 설정되어 있어 비로그인 접근 시 401 반환.
     */
    @PostMapping("/reservations")
    public ResponseEntity<ReservationRes> reserve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReservationReq req
    ) {
        return ResponseEntity.ok(reservationService.reserve(userDetails, req));
    }
}
