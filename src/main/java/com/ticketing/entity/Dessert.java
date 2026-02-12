package com.ticketing.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "desserts")
@Getter
@Builder
@RequiredArgsConstructor
public class Dessert {
    @Id
    @Column(name = "dessert_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;
    private String name;
    private Float price;
    private Integer inventory;
    @Column(name = "purchase_limit")
    private Integer purchaseLimit;
    @Column(name = "open_dt")
    private LocalDateTime openDt;
    @Column(name = "open_status")
    private String openStatus;
}
