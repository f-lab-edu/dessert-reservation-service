package com.ticketing.repository;

import com.ticketing.entity.Reservation;
import com.ticketing.enums.ReserveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 사용자의 특정 디저트에 대해 당일 취소되지 않은 예약 수량 합산.
     * 하루 단위 purchaseLimit 초과 여부 검증 시 사용.
     */
    @Query("SELECT COALESCE(SUM(r.count), 0) FROM reservations r " +
            "WHERE r.user.id = :userId AND r.dessert.id = :dessertId " +
            "AND r.reserveStatus <> :cancelledStatus " +
            "AND CAST(r.createdDt AS date) = :today")
    int sumTodayCountByUserIdAndDessertId(
            @Param("userId") Long userId,
            @Param("dessertId") Long dessertId,
            @Param("cancelledStatus") ReserveStatus cancelledStatus,
            @Param("today") LocalDate today
    );
}
