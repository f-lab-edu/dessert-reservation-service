package com.ticketing.common.util;

import com.ticketing.dto.FcmMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.stereotype.Component;

/**
 * Redis Stream에 FCM 푸시 알림 메시지를 발행하는 Producer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FcmMessageProducer {

    public static final String STREAM_KEY = "fcm:notification:stream";

    private final RedisConstructor redisConstructor;

    /**
     * FCM 메시지를 Redis Stream에 발행.
     * 비동기로 처리되어 즉시 반환.
     */
    public void sendMessage(FcmMessageDto messageDto) {
        try {
            ObjectRecord<String, FcmMessageDto> record = StreamRecords
                    .newRecord()
                    .ofObject(messageDto)
                    .withStreamKey(STREAM_KEY);

            redisConstructor.addStreamMessage(record);
            log.info("FCM 메시지 발행 성공: stream={}, title={}", STREAM_KEY, messageDto.getTitle());
        } catch (Exception e) {
            log.error("FCM 메시지 발행 실패: title={}, error={}", messageDto.getTitle(), e.getMessage(), e);
        }
    }
}
