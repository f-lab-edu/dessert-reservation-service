package com.ticketing.dto;

import com.ticketing.entity.Reservation;
import com.ticketing.enums.ReserveStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReservationRes {
    private Long reservationId;
    private Long dessertId;
    private String dessertName;
    private Integer count;
    private BigDecimal totalPrice;
    private ReserveStatus reserveStatus;

    public static ReservationRes from(Reservation reservation) {
        return ReservationRes.builder()
                .reservationId(reservation.getId())
                .dessertId(reservation.getDessert().getId())
                .dessertName(reservation.getDessert().getName())
                .count(reservation.getCount())
                .totalPrice(reservation.getTotalPrice())
                .reserveStatus(reservation.getReserveStatus())
                .build();
    }
}
