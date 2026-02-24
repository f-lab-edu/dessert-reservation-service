package com.ticketing.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

/**
 * 매장 구독자 정보 DTO.
 * QueryDSL Projection으로 storeId와 사용자 정보를 함께 조회.
 */
@Getter
public class StoreSubscriberDto {
    private final Long storeId;
    private final Long userId;
    private final String pushToken;

    @QueryProjection
    public StoreSubscriberDto(Long storeId, Long userId, String pushToken) {
        this.storeId = storeId;
        this.userId = userId;
        this.pushToken = pushToken;
    }
}
