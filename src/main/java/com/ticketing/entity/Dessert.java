package com.ticketing.entity;

import com.ticketing.enums.OpenStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "desserts")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Dessert{
    @Id
    @Column(name = "dessert_id")
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    private String name;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer inventory;
    @Column(name = "purchase_limit", nullable = false)
    private Integer purchaseLimit;
    @Column(name = "open_dt", nullable = false)
    private LocalDateTime openDt;
    @Enumerated(EnumType.STRING)
    @Column(name = "open_status", nullable = false)
    private OpenStatus openStatus;
}
