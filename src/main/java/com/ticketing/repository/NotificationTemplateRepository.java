package com.ticketing.repository;

import com.ticketing.entity.NotificationTemplate;
import com.ticketing.enums.NotificationKey;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 알림 템플릿 Repository.
 */
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, NotificationKey> {
}
