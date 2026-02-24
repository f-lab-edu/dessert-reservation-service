package com.ticketing.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomDessertRepository {

    /**
     * 특정 시간 범위에 오픈 예정인 디저트의 매장 ID 중복 제거 후 조회.
     * open_dt 인덱스를 활용하여 효율적인 조회 수행.
     * DATE_FORMAT 대신 범위 조회를 사용하여 인덱스 스캔 가능.
     *
     * @param startTime 시작 시간 (이상)
     * @param endTime 종료 시간 (미만)
     * @return 오픈 예정 매장 ID 목록
     */
    List<Long> findDistinctStoreIdsByOpenDtBetween(LocalDateTime startTime, LocalDateTime endTime);
}
