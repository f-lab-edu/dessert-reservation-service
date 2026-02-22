package com.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
