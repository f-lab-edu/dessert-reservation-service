package com.ticketing.common.validation;

import com.ticketing.entity.Dessert;
import com.ticketing.entity.User;
import com.ticketing.enums.OpenStatus;
import com.ticketing.enums.ReserveStatus;
import com.ticketing.exception.BusinessException;
import com.ticketing.exception.ErrorCode;
import com.ticketing.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예약 관련 검증 로직을 담당하는 Validator.
 * 비즈니스 규칙 위반 시 BusinessException을 발생시킴.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    /**
     * 예약 가능 상태 검증.
     * openStatus가 OPEN이고 openDt가 현재 시각 이전인 것만 예약 가능.
     */
    public void validateReservationAvailability(Dessert dessert) {
        if (!OpenStatus.OPEN.equals(dessert.getOpenStatus()) ||
            dessert.getOpenDt().isAfter(LocalDateTime.now())) {
            log.warn("Reservation not available: dessertId={}, status={}, openDt={}",
                     dessert.getId(), dessert.getOpenStatus(), dessert.getOpenDt());
            throw new BusinessException(ErrorCode.RESERVATION_NOT_AVAILABLE);
        }
    }

    /**
     * 구매 한도 검증.
     * 당일 기존 예약 수량 + 이번 요청 수량이 purchaseLimit를 초과하는지 확인.
     * 보안을 위해 정확한 남은 수량은 로그에만 기록하고 사용자에게는 일반 메시지만 제공.
     */
    public void validatePurchaseLimit(User user, Dessert dessert, int requestCount) {
        int existingCount = reservationRepository.sumTodayCountByUserIdAndDessertId(
                user.getId(), dessert.getId(), ReserveStatus.CANCELLED, LocalDate.now()
        );

        if (existingCount + requestCount > dessert.getPurchaseLimit()) {
            log.warn("Purchase limit exceeded: userId={}, dessertId={}, existing={}, requested={}, limit={}",
                     user.getId(), dessert.getId(), existingCount, requestCount, dessert.getPurchaseLimit());
            throw new BusinessException(ErrorCode.PURCHASE_LIMIT_EXCEEDED);
        }
    }

    /**
     * 재고 검증.
     * 재고 부족 시 예외 발생.
     * 보안을 위해 정확한 재고 수량은 로그에만 기록하고 사용자에게는 일반 메시지만 제공.
     */
    public void validateInventory(Dessert dessert, int count) {
        if (dessert.getInventory() < count) {
            log.warn("Insufficient inventory: dessertId={}, dessertName={}, requested={}, available={}",
                     dessert.getId(), dessert.getName(), count, dessert.getInventory());
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
    }
}
