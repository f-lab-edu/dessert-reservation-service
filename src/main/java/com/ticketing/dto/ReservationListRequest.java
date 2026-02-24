package com.ticketing.dto;

import com.ticketing.enums.ReserveStatus;

public record ReservationListRequest(
        ReserveStatus reserveStatus,  // 예약 상태 필터 (optional)
        SortBy sortBy,                // 정렬 기준 (기본값: RESERVE_DATE)
        SortDirection sortDirection   // 정렬 방향 (기본값: DESC)
) {
    /**
     * 정렬 기준 enum.
     */
    public enum SortBy {
        RESERVE_DATE,  // 예약 날짜 기준 정렬
        OPEN_DATE      // 오픈 날짜 기준 정렬
    }

    /**
     * 정렬 방향 enum.
     */
    public enum SortDirection {
        ASC,   // 오름차순
        DESC   // 내림차순
    }

    /**
     * 기본값 적용. sortBy와 sortDirection이 null인 경우 기본값 설정.
     */
    public ReservationListRequest {
        if (sortBy == null) {
            sortBy = SortBy.RESERVE_DATE;
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.DESC;
        }
    }
}
