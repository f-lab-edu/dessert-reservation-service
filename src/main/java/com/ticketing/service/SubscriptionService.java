package com.ticketing.service;

import com.ticketing.dto.StoreRes;

import java.util.List;

public interface SubscriptionService {
    /**
     * 상점 구독 토글.
     * 이미 구독 중이면 구독 취소, 구독하지 않았으면 새로 구독 추가.
     */
    void toggleSubscription(Long userId, Long storeId);

    /**
     * 구독 중인 상점 리스트 조회.
     */
    List<StoreRes> getSubscriptionList(Long userId);
}
