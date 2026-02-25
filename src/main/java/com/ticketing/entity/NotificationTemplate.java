package com.ticketing.entity;

import com.ticketing.common.entity.BaseEntity;
import com.ticketing.enums.NotificationKey;
import com.ticketing.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 템플릿 엔티티.
 * FCM 푸시 알림 발송 시 사용할 제목, 본문, 링크 URL 등을 관리.
 */
@Entity(name = "notification_template")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class NotificationTemplate extends BaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "template_key", nullable = false)
    private NotificationKey templateKey;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "noti_type", nullable = false)
    private NotificationType notiType;

    @Column(nullable = false)
    private String url;
}
