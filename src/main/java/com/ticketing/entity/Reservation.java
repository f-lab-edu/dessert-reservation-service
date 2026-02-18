package com.ticketing.entity;

import com.ticketing.common.entity.BaseEntity;
import com.ticketing.enums.ReserveStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity(name = "reservations")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Reservation extends BaseEntity {
    @Id
    @Column(name = "reservation_id")
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_id")
    private Dessert dessert;
    private Integer count;
    private Integer totalPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "reserve_status")
    private ReserveStatus reserveStatus;
}
