package com.ticketing.repository;

import com.ticketing.dto.ReservationListRequest.SortBy;
import com.ticketing.dto.ReservationListRequest.SortDirection;
import com.ticketing.entity.Reservation;
import com.ticketing.enums.ReserveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomReservationRepository {
    /**
     * 사용자의 예약 리스트 조회. 예약 상태로 필터링하고, 예약 날짜 또는 오픈 날짜로 정렬.
     * Page 객체로 반환하여 전체 개수 정보 포함.
     */
    Page<Reservation> findByUserIdWithFilters(
            Long userId,
            ReserveStatus reserveStatus,
            SortBy sortBy,
            SortDirection sortDirection,
            Pageable pageable
    );
}
