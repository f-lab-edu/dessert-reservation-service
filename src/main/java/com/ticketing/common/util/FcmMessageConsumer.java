package com.ticketing.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.dto.FcmMessageDto;
import com.ticketing.service.FcmService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Redis Stream에서 FCM 푸시 알림 메시지를 소비하는 Consumer.
 * 애플리케이션 시작 시 자동으로 Consumer Group 생성 및 메시지 수신 시작.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FcmMessageConsumer implements StreamListener<String, MapRecord<String, Object, Object>> {

    private static final String STREAM_KEY = FcmMessageProducer.STREAM_KEY;

    @Value("${redis.stream.fcm.consumer-group}")
    private String consumerGroup;

    @Value("${redis.stream.fcm.consumer-name}")
    private String consumerName;

    @Value("${redis.stream.fcm.max-retry}")
    private int maxRetry;

    @Value("${redis.stream.fcm.dlq-key}")
    private String dlqKey;

    private final RedisOperator redisOperator;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
    private Subscription subscription;

    /**
     * 애플리케이션 시작 시 Consumer Group 생성 및 메시지 수신 시작.
     */
    @PostConstruct
    public void start() {
        // Consumer Group 생성 (없으면 생성)
        redisOperator.createStreamConsumerGroup(STREAM_KEY, consumerGroup);

        // Listener Container 생성 및 시작
        listenerContainer = redisOperator.createStreamMessageListenerContainer();
        listenerContainer.start();

        // Consumer 등록 (새로운 메시지만 읽음)
        subscription = listenerContainer.receive(
                Consumer.from(consumerGroup, consumerName),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                this
        );

        log.info("FCM Message Consumer 시작: stream={}, group={}, consumer={}",
                STREAM_KEY, consumerGroup, consumerName);
    }

    /**
     * 애플리케이션 종료 시 리소스 정리.
     */
    @PreDestroy
    public void stop() {
        if (subscription != null) {
            subscription.cancel();
        }
        if (listenerContainer != null) {
            listenerContainer.stop();
        }
        log.info("FCM Message Consumer 종료");
    }

    /**
     * Stream에서 메시지 수신 시 호출.
     * FCM 푸시 알림 전송 후 ACK 처리.
     * 실패 시 재시도 횟수 추적하여 임계값 초과 시 DLQ로 이동.
     */
    @Override
    public void onMessage(MapRecord<String, Object, Object> message) {
        String messageId = message.getId().getValue();

        try {
            // 메시지를 FcmMessageDto로 변환 (objectMapper 사용하여 타입 안전성 보장)
            FcmMessageDto fcmMessage = FcmMessageDto.from(message, objectMapper);

            // FCM 푸시 알림 전송
            fcmService.sendMessage(fcmMessage);

            // 성공 시 ACK 및 실패 횟수 삭제
            redisOperator.ackStream(STREAM_KEY, consumerGroup, message);
            redisOperator.deleteFailureCount(messageId);

            log.info("FCM 메시지 처리 완료: messageId={}, title={}",
                    messageId, fcmMessage.getTitle());
        } catch (Exception e) {
            // 실패 횟수 증가
            int failureCount = redisOperator.incrementFailureCount(messageId);

            log.error("FCM 메시지 처리 실패: messageId={}, failureCount={}, error={}",
                    messageId, failureCount, e.getMessage(), e);

            // 최대 재시도 횟수 초과 시 DLQ로 이동
            if (failureCount >= maxRetry) {
                log.warn("최대 재시도 횟수 초과 - DLQ로 이동: messageId={}, failureCount={}",
                        messageId, failureCount);
                redisOperator.moveToDeadLetterQueue(STREAM_KEY, consumerGroup, message, dlqKey);
            }
            // ACK를 하지 않으면 메시지가 Pending 상태로 남아 재처리 대상이 됨
        }
    }

    /**
     * Pending 메시지 재처리 스케줄러.
     * 5분마다 실행되어 1분 이상 pending 상태인 메시지를 재전송 시도.
     */
    @Scheduled(fixedDelay = 300000) // 5분마다 실행
    public void processPendingMessages() {
        try {
            // Pending 메시지 조회
            PendingMessages pendingMessages = redisOperator.findStreamPendingMessages(
                    STREAM_KEY, consumerGroup, consumerName);

            if (pendingMessages.isEmpty()) {
                log.debug("Pending 메시지 없음: stream={}, group={}", STREAM_KEY, consumerGroup);
                return;
            }

            log.info("Pending 메시지 발견: count={}, stream={}, group={}",
                    pendingMessages.size(), STREAM_KEY, consumerGroup);

            for (PendingMessage pendingMessage : pendingMessages) {
                try {
                    String messageId = pendingMessage.getIdAsString();

                    // 1분 이상 pending 상태인 메시지만 재처리
                    if (pendingMessage.getElapsedTimeSinceLastDelivery().toMillis() > 60000) {
                        // 현재 실패 횟수 조회
                        int failureCount = redisOperator.getFailureCount(messageId);

                        // 최대 재시도 횟수 초과 시 DLQ로 이동
                        if (failureCount >= maxRetry) {
                            log.warn("Pending 메시지 최대 재시도 횟수 초과 - DLQ로 이동: messageId={}, failureCount={}",
                                    messageId, failureCount);

                            // Claim 후 DLQ로 이동
                            List<MapRecord<String, Object, Object>> claimedMessages =
                                    redisOperator.claimPendingMessages(
                                            STREAM_KEY,
                                            consumerGroup,
                                            consumerName,
                                            Duration.ofMillis(60000),
                                            pendingMessage.getId()
                                    );

                            for (MapRecord<String, Object, Object> claimedMessage : claimedMessages) {
                                redisOperator.moveToDeadLetterQueue(STREAM_KEY, consumerGroup, claimedMessage, dlqKey);
                            }
                            continue;
                        }

                        // Pending 메시지를 claim하여 소유권 이전
                        List<MapRecord<String, Object, Object>> claimedMessages =
                                redisOperator.claimPendingMessages(
                                        STREAM_KEY,
                                        consumerGroup,
                                        consumerName,
                                        Duration.ofMillis(60000),
                                        pendingMessage.getId()
                                );

                        // Claim된 메시지 재처리
                        for (MapRecord<String, Object, Object> claimedMessage : claimedMessages) {
                            log.info("Pending 메시지 재처리 시도: messageId={}, failureCount={}",
                                    claimedMessage.getId(), failureCount);
                            onMessage(claimedMessage);
                        }
                    }
                } catch (Exception e) {
                    log.error("Pending 메시지 재처리 실패: messageId={}, error={}",
                            pendingMessage.getIdAsString(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Pending 메시지 조회 실패: stream={}, group={}, error={}",
                    STREAM_KEY, consumerGroup, e.getMessage(), e);
        }
    }
}
