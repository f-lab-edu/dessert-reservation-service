package com.ticketing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity(name = "reservations")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @Column(name = "reservation_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Dessert dessert;
    private Integer count;
    private Integer totalPrice;
    @Column(name = "reserve_status")
    private String reserveStatus;
    @Column(name = "created_dt")
    @CreatedDate
    private LocalDateTime createdDt;
}
