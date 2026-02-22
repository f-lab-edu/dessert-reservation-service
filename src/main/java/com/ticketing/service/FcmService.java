package com.ticketing.service;

import com.ticketing.dto.FcmMessageDto;

/**
 * FCM 푸시 알림 전송 서비스.
 */
public interface FcmService {

    /**
     * 단일 사용자에게 FCM 푸시 알림 전송.
     */
    void sendMessage(FcmMessageDto messageDto);
}
