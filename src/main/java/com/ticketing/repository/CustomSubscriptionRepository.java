package com.ticketing.repository;

import com.ticketing.dto.StoreSubscriberDto;

import java.util.List;

public interface CustomSubscriptionRepository {

    /**
     * 여러 매장의 구독자 정보를 단일 쿼리로 조회.
     * push_token이 있고 삭제되지 않은 사용자만 반환.
     */
    List<StoreSubscriberDto> findSubscribersByStoreIds(List<Long> storeIds);
}
