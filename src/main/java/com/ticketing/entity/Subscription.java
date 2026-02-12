package com.ticketing.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity(name = "subscriptions")
@Getter
@Builder
@RequiredArgsConstructor
@IdClass(SubscriptionPk.class)
public class Subscription {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "store_id")
    private Long storeId;
    @Column(name = "created_dt")
    @CreatedDate
    private LocalDateTime createdDt;
}
