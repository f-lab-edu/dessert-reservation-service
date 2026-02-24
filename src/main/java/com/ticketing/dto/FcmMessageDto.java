package com.ticketing.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.util.Map;

/**
 * FCM 푸시 알림 메시지 DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageDto {
    private String targetToken;
    private String title;
    private String body;

    /**
     * Redis Stream MapRecord를 FcmMessageDto로 변환.
     * objectMapper를 사용하여 타입 안전성 보장.
     */
    public static FcmMessageDto from(MapRecord<String, Object, Object> message, ObjectMapper objectMapper) {
        Map<Object, Object> value = message.getValue();
        return objectMapper.convertValue(value, FcmMessageDto.class);
    }
}
