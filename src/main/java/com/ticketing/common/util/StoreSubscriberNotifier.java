package com.ticketing.common.util;

import com.ticketing.dto.FcmMessageDto;
import com.ticketing.dto.StoreSubscriberDto;
import com.ticketing.entity.NotificationTemplate;
import com.ticketing.enums.NotificationKey;
import com.ticketing.exception.BusinessException;
import com.ticketing.exception.ErrorCode;
import com.ticketing.repository.NotificationTemplateRepository;
import com.ticketing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 스토어 구독자들에게 FCM 푸시 알림을 발송하는 유틸리티.
 * 비즈니스 로직이 아닌 기술적인 알림 발송 작업만 수행.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreSubscriberNotifier {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final FcmMessageProducer fcmMessageProducer;

    /**
     * 단일 스토어 구독자에게 알림 발송.
     * 디저트 신규 등록 시 사용.
     *
     * @return 발송 성공한 메시지 수
     */
    public int notifySubscribers(Long storeId, NotificationKey notificationKey) {
        return notifySubscribers(List.of(storeId), notificationKey);
    }

    /**
     * 여러 스토어 구독자에게 알림 발송.
     * 스케줄러에서 일괄 처리 시 사용.
     *
     * @return 발송 성공한 메시지 수
     */
    public int notifySubscribers(List<Long> storeIds, NotificationKey notificationKey) {
        if (storeIds.isEmpty()) {
            log.debug("storeIds가 비어있어 알림을 발송하지 않음");
            return 0;
        }

        // NotificationTemplate 조회
        NotificationTemplate template = notificationTemplateRepository.findById(notificationKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "알림 템플릿"));

        List<StoreSubscriberDto> subscribers =
                subscriptionRepository.findSubscribersByStoreIds(storeIds);

        if (subscribers.isEmpty()) {
            log.info("구독자 없음. storeIds={}", storeIds);
            return 0;
        }

        log.info("알림 발송 시작. 대상 구독자 수: {}, storeIds={}, notificationKey={}",
                subscribers.size(), storeIds, notificationKey);

        int successCount = 0;

        for (StoreSubscriberDto subscriber : subscribers) {
            try {
                FcmMessageDto message = FcmMessageDto.builder()
                        .targetToken(subscriber.getPushToken())
                        .title(template.getTitle())
                        .body(template.getBody())
                        .build();

                fcmMessageProducer.sendMessage(message);
                successCount++;
            } catch (Exception e) {
                log.error("메시지 발송 실패. storeId={}, userId={}",
                        subscriber.getStoreId(), subscriber.getUserId(), e);
            }
        }

        log.info("알림 발송 완료. 성공: {}/{}", successCount, subscribers.size());
        return successCount;
    }
}
