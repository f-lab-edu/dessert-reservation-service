package com.ticketing.scheduler;

import com.ticketing.common.util.FcmMessageProducer;
import com.ticketing.dto.FcmMessageDto;
import com.ticketing.dto.StoreSubscriberDto;
import com.ticketing.repository.DessertRepository;
import com.ticketing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 디저트 오픈 상태를 자동으로 업데이트하는 스케줄러.
 * 15분마다 실행되어 PENDING 상태의 디저트 중 오픈 시각이 도래한 것을 OPEN으로 변경.
 * scheduler.dessert-open.enabled 설정으로 활성화/비활성화 제어.
 */
@Component
@ConditionalOnProperty(
    value = "scheduler.dessert-open.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@RequiredArgsConstructor
@Slf4j
public class DessertOpenScheduler {

    private final DessertRepository dessertRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FcmMessageProducer fcmMessageProducer;

    /**
     * 15분마다 실행되어 디저트 상태를 PENDING에서 OPEN으로 변경.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void updateDessertOpenStatus() {
        log.info("디저트 오픈 상태 업데이트 배치 시작");

        int updatedCount = dessertRepository.updatePendingToOpenByOpenDt();

        log.info("디저트 오픈 상태 업데이트 완료. 업데이트된 디저트 수: {}", updatedCount);
    }

    /**
     * 15분마다 실행되어 PENDING 상태인 디저트 중 오픈시간이 15분 남은 디저트에 대해 오픈 알림 전송.
     * 해당 디저트의 storeId를 구하고, 해당 store를 구독하는 사용자에게 FCM 푸시 알림 발송.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional(readOnly = true)
    public void sendNotiBeforeOpen() {
        log.info("디저트 오픈 알림 메시지 생성 시작");

        // 15분 후 오픈 예정 시간 범위 계산 (인덱스 활용을 위한 범위 조회)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.plusMinutes(15);
        LocalDateTime startTime = targetTime.withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(1);

        // 15분 후 오픈 예정인 디저트의 distinct storeId 조회 (범위 검색으로 인덱스 활용)
        List<Long> storeIds = dessertRepository.findDistinctStoreIdsByOpenDtBetween(startTime, endTime);

        if (storeIds.isEmpty()) {
            log.info("15분 후 오픈 예정인 디저트 없음");
            return;
        }

        log.info("15분 후 오픈 예정 매장 수: {}", storeIds.size());

        List<StoreSubscriberDto> subscribers = subscriptionRepository.findSubscribersByStoreIds(storeIds);

        if (subscribers.isEmpty()) {
            log.info("구독자 없음");
            return;
        }

        log.info("총 구독자 수: {}", subscribers.size());

        // storeId별로 그룹핑
        Map<Long, List<StoreSubscriberDto>> subscribersByStore = subscribers.stream()
                .collect(Collectors.groupingBy(StoreSubscriberDto::getStoreId));

        int totalMessageCount = 0;

        // 각 store의 구독자에게 알림 발송
        for (Long storeId : storeIds) {
            List<StoreSubscriberDto> storeSubscribers = subscribersByStore.getOrDefault(storeId, Collections.emptyList());

            if (storeSubscribers.isEmpty()) {
                log.debug("storeId={}의 구독자 없음", storeId);
                continue;
            }

            log.info("storeId={}의 구독자 수: {}", storeId, storeSubscribers.size());

            // 각 구독자에게 FCM 메시지 발송
            for (StoreSubscriberDto subscriber : storeSubscribers) {
                try {
                    FcmMessageDto message = FcmMessageDto.builder()
                            .targetToken(subscriber.getPushToken())
                            .title("디저트 오픈 알림")
                            .body("구독하신 매장의 디저트가 15분 후 오픈됩니다!")
                            .build();

                    fcmMessageProducer.sendMessage(message);
                    totalMessageCount++;
                } catch (Exception e) {
                    log.error("메시지 발송 실패. storeId={}, userId={}", storeId, subscriber.getUserId(), e);
                }
            }
        }

        log.info("디저트 오픈 알림 메시지 생성 완료. 발송된 메시지 수: {}", totalMessageCount);
    }
}
