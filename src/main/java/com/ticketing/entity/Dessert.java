package com.ticketing.entity;

import com.ticketing.enums.OpenStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "desserts")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Dessert {
    @Id
    @Column(name = "dessert_id")
    @GeneratedValue
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
    @Enumerated(EnumType.STRING)
    @Column(name = "open_status")
    private OpenStatus openStatus;
}
