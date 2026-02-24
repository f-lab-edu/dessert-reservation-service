package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.common.dto.PaginatedDto;
import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.ReservationListRequest;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;
import com.ticketing.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController extends BaseController {

    private final ReservationService reservationService;

    /**
     * 로그인한 사용자의 디저트 예약 리스트 조회.
     * 예약 상태로 필터링하고, 예약 날짜 또는 오픈 날짜로 정렬 가능.
     * 페이지네이션 적용 (기본값: page=0, size=10).
     */
    @GetMapping("/reservations")
    public ResponseEntity<PaginatedDto<ReservationRes>> getReservationList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute ReservationListRequest req,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.getReservationList(userDetails, req, pageable));
    }

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
