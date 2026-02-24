package com.ticketing.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * Redis Stream 관련 유틸리티.
 * Consumer Group 생성, ACK 처리, Pending Message 조회 등의 기능 제공.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisOperator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${redis.stream.fcm.failures-key}")
    private String failuresKey;

    /**
     * Stream Consumer Group 생성.
     * Stream이 없으면 생성하고, Consumer Group도 함께 생성.
     * try-with-resources로 Connection 자동 반환하여 리소스 누수 방지.
     */
    public void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
        try {
            // Stream이 존재하지 않으면 Stream과 Consumer Group을 함께 생성
            if (Boolean.FALSE.equals(this.redisTemplate.hasKey(streamKey))) {
                // try-with-resources로 Connection 자동 close
                try (org.springframework.data.redis.connection.RedisConnection connection =
                        this.redisTemplate.getConnectionFactory().getConnection()) {

                    RedisAsyncCommands commands = (RedisAsyncCommands) connection.getNativeConnection();

                    CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                            .add(CommandKeyword.CREATE)
                            .add(streamKey)
                            .add(consumerGroupName)
                            .add("0")
                            .add("MKSTREAM");

                    commands.dispatch(CommandType.XGROUP, new StatusOutput(StringCodec.UTF8), args);
                    log.info("Stream과 Consumer Group 생성 완료: streamKey={}, group={}", streamKey, consumerGroupName);
                }
            }
            // Stream이 존재하면 Consumer Group만 생성
            else {
                try {
                    if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
                        this.redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
                        log.info("Consumer Group 생성 완료: streamKey={}, group={}", streamKey, consumerGroupName);
                    } else {
                        log.debug("Consumer Group이 이미 존재: streamKey={}, group={}", streamKey, consumerGroupName);
                    }
                } catch (Exception e) {
                    // BUSYGROUP 에러는 이미 존재한다는 의미이므로 무시
                    if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                        log.debug("Consumer Group이 이미 존재 (BUSYGROUP): streamKey={}, group={}", streamKey, consumerGroupName);
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Consumer Group 생성 실패: streamKey={}, group={}, error={}",
                      streamKey, consumerGroupName, e.getMessage(), e);
            throw new RuntimeException("Consumer Group 생성 실패", e);
        }
    }

    /**
     * Consumer Group이 존재하는지 확인.
     */
    public boolean isStreamConsumerGroupExist(String streamKey, String consumerGroupName) {
        Iterator<StreamInfo.XInfoGroup> iterator = this.redisTemplate
                .opsForStream().groups(streamKey).stream().iterator();

        while (iterator.hasNext()) {
            StreamInfo.XInfoGroup xInfoGroup = iterator.next();
            if (xInfoGroup.groupName().equals(consumerGroupName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 메시지 처리 완료 확인 (ACK).
     * streamKey, consumerGroup, messageId를 모두 사용하여 ACK 처리.
     */
    public void ackStream(String streamKey, String consumerGroupName, MapRecord<String, Object, Object> message) {
        try {
            Long acknowledgedCount = this.redisTemplate.opsForStream()
                    .acknowledge(streamKey, consumerGroupName, message.getId());

            if (acknowledgedCount == null || acknowledgedCount == 0) {
                log.warn("메시지 ACK 실패 (이미 처리됨 또는 존재하지 않음): streamKey={}, group={}, messageId={}",
                        streamKey, consumerGroupName, message.getId());
            } else {
                log.debug("메시지 ACK 성공: streamKey={}, group={}, messageId={}",
                        streamKey, consumerGroupName, message.getId());
            }
        } catch (Exception e) {
            log.error("메시지 ACK 처리 중 에러: streamKey={}, group={}, messageId={}, error={}",
                    streamKey, consumerGroupName, message.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Pending 상태인 메시지 조회.
     * 처리 중 실패한 메시지를 재처리하기 위해 사용.
     */
    public PendingMessages findStreamPendingMessages(String streamKey, String consumerGroupName, String consumerName) {
        return this.redisTemplate.opsForStream()
                .pending(streamKey, Consumer.from(consumerGroupName, consumerName), org.springframework.data.domain.Range.unbounded(), 100L);
    }

    /**
     * Pending 메시지를 claim하여 소유권을 현재 Consumer로 이전.
     * 일정 시간 이상 pending 상태인 메시지를 재처리하기 위해 사용.
     */
    public List<MapRecord<String, Object, Object>> claimPendingMessages(
            String streamKey,
            String consumerGroupName,
            String consumerName,
            Duration minIdleTime,
            RecordId... recordIds) {
        try {
            return this.redisTemplate.opsForStream()
                    .claim(streamKey, consumerGroupName, consumerName, minIdleTime, recordIds);
        } catch (Exception e) {
            log.error("Pending 메시지 claim 실패: streamKey={}, group={}, consumer={}, error={}",
                    streamKey, consumerGroupName, consumerName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Stream에 메시지 추가.
     * Producer가 메시지를 발행할 때 사용.
     */
    public <T> RecordId addStreamMessage(ObjectRecord<String, T> record) {
        return this.redisTemplate.opsForStream().add(record);
    }

    /**
     * StreamMessageListenerContainer 생성.
     * Consumer가 메시지를 수신할 때 사용.
     * RedisTemplate과 동일한 GenericJackson2JsonRedisSerializer를 사용하여 직렬화 일관성 보장.
     */
    public StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> createStreamMessageListenerContainer(){
        // RedisTemplate과 동일한 Serializer 사용 (직렬화 일관성)
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        return StreamMessageListenerContainer.create(
                this.redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions.builder()
                        .hashKeySerializer(new StringRedisSerializer())
                        .hashValueSerializer(jsonSerializer)  // String → JSON 직렬화로 변경
                        .pollTimeout(Duration.ofMillis(100))
                        .errorHandler((error) -> {  // 에러 핸들러 추가
                            log.error("Redis Stream Container 오류: message={}",
                                    error.getMessage(), error);
                        })
                        .build()
        );
    }

    /**
     * 메시지 실패 횟수 조회.
     * Redis Hash에 저장된 실패 횟수 반환 (없으면 0).
     */
    public int getFailureCount(String messageId) {
        Object count = this.redisTemplate.opsForHash().get(failuresKey, messageId);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    /**
     * 메시지 실패 횟수 증가.
     * 실패 시마다 호출하여 횟수 추적.
     */
    public int incrementFailureCount(String messageId) {
        Long newCount = this.redisTemplate.opsForHash().increment(failuresKey, messageId, 1);
        // TTL 설정 (7일 후 자동 삭제)
        this.redisTemplate.expire(failuresKey, Duration.ofDays(7));
        return newCount.intValue();
    }

    /**
     * 메시지 실패 횟수 삭제.
     * 성공적으로 처리된 메시지의 실패 횟수 제거.
     */
    public void deleteFailureCount(String messageId) {
        this.redisTemplate.opsForHash().delete(failuresKey, messageId);
    }

    /**
     * DLQ(Dead Letter Queue)로 메시지 이동.
     * 반복적으로 실패한 메시지를 별도 Stream으로 이동 후 원본 스트림에서 ACK 처리.
     */
    public void moveToDeadLetterQueue(String originalStreamKey, String consumerGroupName, MapRecord<String, Object, Object> message, String dlqKey) {
        try {
            // DLQ Stream에 메시지 추가 (원본 데이터 보존)
            this.redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .in(dlqKey)
                            .ofMap(message.getValue())
            );

            // 원본 메시지 ACK (Stream에서 제거)
            ackStream(originalStreamKey, consumerGroupName, message);

            // 실패 횟수 삭제
            deleteFailureCount(message.getId().getValue());

            log.warn("메시지를 DLQ로 이동: messageId={}, dlq={}", message.getId(), dlqKey);
        } catch (Exception e) {
            log.error("DLQ 이동 실패: messageId={}, error={}", message.getId(), e.getMessage(), e);
        }
    }
}
