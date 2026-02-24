package com.ticketing.service;

import com.ticketing.common.dto.PaginatedDto;
import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.ReservationListRequest;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    /**
     * 로그인한 사용자의 디저트 예약 리스트 조회.
     * 예약 상태로 필터링하고, 예약 날짜 또는 오픈 날짜로 정렬.
     */
    PaginatedDto<ReservationRes> getReservationList(CustomUserDetails userDetails, ReservationListRequest req, Pageable pageable);

    /**
     * 로그인한 사용자의 디저트 예약 요청 처리.
     */
    ReservationRes reserve(CustomUserDetails userDetails, ReservationReq req);
}
