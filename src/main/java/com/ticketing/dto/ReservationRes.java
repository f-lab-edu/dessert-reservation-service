package com.ticketing.dto;

import com.ticketing.entity.Reservation;
import com.ticketing.enums.ReserveStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationRes {
    private Long reservationId;
    private Long dessertId;
    private String dessertName;
    private String storeName;
    private Integer count;
    private BigDecimal totalPrice;
    private ReserveStatus reserveStatus;
    private LocalDateTime createdDt;  // 예약 날짜
    private LocalDateTime openDt;     // 오픈 날짜

    public static ReservationRes from(Reservation reservation) {
        return ReservationRes.builder()
                .reservationId(reservation.getId())
                .dessertId(reservation.getDessert().getId())
                .dessertName(reservation.getDessert().getName())
                .storeName(reservation.getDessert().getStore().getName())
                .count(reservation.getCount())
                .totalPrice(reservation.getTotalPrice())
                .reserveStatus(reservation.getReserveStatus())
                .createdDt(reservation.getCreatedDt())
                .openDt(reservation.getDessert().getOpenDt())
                .build();
    }
}
