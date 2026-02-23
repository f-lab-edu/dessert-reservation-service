package com.ticketing.entity;

import com.ticketing.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "subscriptions")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@IdClass(SubscriptionPk.class)
public class Subscription extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "store_id")
    private Long storeId;
    private LocalDateTime deletedDt;
}
