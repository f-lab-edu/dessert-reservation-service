package com.ticketing.service.impl;

import com.ticketing.dto.StoreRes;
import com.ticketing.entity.Subscription;
import com.ticketing.repository.StoreRepository;
import com.ticketing.repository.SubscriptionRepository;
import com.ticketing.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StoreRepository storeRepository;

    /**
     * 상점 구독 토글.
     * 이미 구독 중인 경우 구독 취소 (삭제), 구독하지 않은 경우 새로 구독 추가.
     */
    @Override
    @Transactional
    public void toggleSubscription(Long userId, Long storeId) {
        // 상점 존재 여부 검증
        if (!storeRepository.existsById(storeId)) {
            throw new EntityNotFoundException("존재하지 않는 상점입니다.");
        }

        // 이미 구독 중인지 확인
        boolean isSubscribed = subscriptionRepository.existsByUserIdAndStoreId(userId, storeId);

        if (isSubscribed) {
            // 구독 중이면 삭제
            subscriptionRepository.deleteByUserIdAndStoreId(userId, storeId);
        } else {
            // 구독하지 않았으면 새로 추가
            Subscription subscription = Subscription.builder()
                    .userId(userId)
                    .storeId(storeId)
                    .createdDt(LocalDateTime.now())
                    .build();
            subscriptionRepository.save(subscription);
        }
    }

    /**
     * 사용자가 구독한 상점 리스트 조회.
     * Subscription 에서 storeId를 추출하여 Store 엔티티를 조회 후 StoreRes로 변환.
     */
    @Override
    public List<StoreRes> getSubscriptionList(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);

        List<Long> storeIds = subscriptions.stream()
                .map(Subscription::getStoreId)
                .toList();

        if (storeIds.isEmpty()) {
            return List.of();
        }

        return storeRepository.findAllById(storeIds).stream()
                .map(store -> StoreRes.from(store, null))
                .toList();
    }
}
