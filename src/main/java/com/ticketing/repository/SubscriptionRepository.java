package com.ticketing.repository;

import com.ticketing.entity.Subscription;
import com.ticketing.entity.SubscriptionPk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionPk> {
    /**
     * 사용자가 특정 상점을 이미 구독했는지 확인.
     */
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    /**
     * 사용자가 구독한 모든 상점 목록 조회.
     */
    List<Subscription> findAllByUserId(Long userId);

    /**
     * 사용자의 특정 상점 구독 삭제.
     */
    void deleteByUserIdAndStoreId(Long userId, Long storeId);
}
