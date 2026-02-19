package com.ticketing.service.impl;

import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;
import com.ticketing.entity.Dessert;
import com.ticketing.entity.Reservation;
import com.ticketing.entity.User;
import com.ticketing.enums.OpenStatus;
import com.ticketing.enums.ReserveStatus;
import com.ticketing.repository.DessertRepository;
import com.ticketing.repository.ReservationRepository;
import com.ticketing.repository.UserRepository;
import com.ticketing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final UserRepository userRepository;
    private final DessertRepository dessertRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 로그인한 사용자의 디저트 예약 처리.
     * 비관적 락으로 재고 동시성 제어 후, 예약 가능 상태·구매 한도·재고를 순서대로 검증.
     */
    @Override
    @Transactional
    public ReservationRes reserve(CustomUserDetails userDetails, ReservationReq req) {
        // 사용자 조회
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        // 디저트 조회 - 비관적 락(SELECT FOR UPDATE)으로 동시 예약 시 재고 정합성 보장
        Dessert dessert = dessertRepository.findByIdWithPessimisticLock(req.dessertId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 디저트입니다."));

        // 예약 가능 상태 검증: openStatus가 OPEN이고 openDt가 현재 시각 이전인 것만 예약 가능
        if (!OpenStatus.OPEN.equals(dessert.getOpenStatus()) || dessert.getOpenDt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 예약할 수 없는 디저트입니다.");
        }

        // 구매 한도 검증: 당일 기존 예약 수량 + 이번 요청 수량이 purchaseLimit 초과 시 거부
        int existingCount = reservationRepository.sumTodayCountByUserIdAndDessertId(
                user.getId(), dessert.getId(), ReserveStatus.CANCELLED, LocalDate.now()
        );
        if (existingCount + req.count() > dessert.getPurchaseLimit()) {
            int remaining = dessert.getPurchaseLimit() - existingCount;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("구매 한도를 초과했습니다. 남은 구매 가능 수량: %d개", remaining));
        }

        // 재고 차감 (재고 부족 시 IllegalArgumentException → 400 변환)
        try {
            dessert.decreaseInventory(req.count());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .dessert(dessert)
                .count(req.count())
                .totalPrice(dessert.getPrice().multiply(BigDecimal.valueOf(req.count())))
                .reserveStatus(ReserveStatus.CONFIRMED)
                .build();

        return ReservationRes.from(reservationRepository.save(reservation));
    }
}
