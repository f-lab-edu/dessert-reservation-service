package com.ticketing.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.ticketing.dto.FcmMessageDto;
import com.ticketing.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * FCM 푸시 알림 전송 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmServiceImpl implements FcmService {

    private final FirebaseMessaging firebaseMessaging;

    /**
     * 단일 사용자에게 FCM 푸시 알림 전송.
     * targetToken을 사용하여 특정 디바이스로 메시지 전송.
     */
    @Override
    public void sendMessage(FcmMessageDto messageDto) {
        if (messageDto.getTargetToken() == null || messageDto.getTargetToken().isEmpty()) {
            log.warn("푸시 알림 전송 실패: targetToken이 없습니다.");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(messageDto.getTitle())
                .setBody(messageDto.getBody())
                .build();

        Message message = Message.builder()
                .setToken(messageDto.getTargetToken())
                .setNotification(notification)
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("FCM 메시지 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패: token={}, title={}, error={}",
                    messageDto.getTargetToken(), messageDto.getTitle(), e.getMessage());
        }
    }
}
