package com.ticketing.common.validator;

import com.ticketing.entity.Dessert;
import com.ticketing.entity.User;
import com.ticketing.enums.OpenStatus;
import com.ticketing.enums.ReserveStatus;
import com.ticketing.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예약 관련 검증 로직을 담당하는 Validator.
 */
@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    /**
     * 예약 가능 상태 검증.
     * openStatus가 OPEN이고 openDt가 현재 시각 이전인 것만 예약 가능.
     */
    public void validateReservationAvailability(Dessert dessert) {
        if (!OpenStatus.OPEN.equals(dessert.getOpenStatus()) || dessert.getOpenDt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 예약할 수 없는 디저트입니다.");
        }
    }

    /**
     * 구매 한도 검증.
     * 당일 기존 예약 수량 + 이번 요청 수량이 purchaseLimit를 초과하는지 확인.
     */
    public void validatePurchaseLimit(User user, Dessert dessert, int requestCount) {
        int existingCount = reservationRepository.sumTodayCountByUserIdAndDessertId(
                user.getId(), dessert.getId(), ReserveStatus.CANCELLED, LocalDate.now()
        );

        if (existingCount + requestCount > dessert.getPurchaseLimit()) {
            int remaining = dessert.getPurchaseLimit() - existingCount;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("구매 한도를 초과했습니다. 남은 구매 가능 수량: %d개", remaining));
        }
    }

    /**
     * 재고 검증.
     * 재고 부족 시 예외 발생.
     */
    public void validateInventory(Dessert dessert, int count) {
        if (dessert.getInventory() < count) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("재고가 부족합니다. 현재 재고: %d개", dessert.getInventory()));
        }
    }
}
