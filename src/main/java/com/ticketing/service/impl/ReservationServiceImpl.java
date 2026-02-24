package com.ticketing.service.impl;

import com.ticketing.common.dto.PaginatedDto;
import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.common.validation.ReservationValidator;
import com.ticketing.dto.ReservationListRequest;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;
import com.ticketing.entity.Dessert;
import com.ticketing.entity.Reservation;
import com.ticketing.entity.User;
import com.ticketing.enums.ReserveStatus;
import com.ticketing.exception.BusinessException;
import com.ticketing.exception.ErrorCode;
import com.ticketing.repository.DessertRepository;
import com.ticketing.repository.ReservationRepository;
import com.ticketing.repository.UserRepository;
import com.ticketing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final UserRepository userRepository;
    private final DessertRepository dessertRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;

    /**
     * 로그인한 사용자의 디저트 예약 리스트 조회.
     * 예약 상태로 필터링하고, 예약 날짜 또는 오픈 날짜로 정렬.
     */
    @Override
    public PaginatedDto<ReservationRes> getReservationList(
            CustomUserDetails userDetails,
            ReservationListRequest req,
            Pageable pageable
    ) {
        Page<Reservation> page = reservationRepository.findByUserIdWithFilters(
                userDetails.getId(),
                req.reserveStatus(),
                req.sortBy(),
                req.sortDirection(),
                pageable
        );

        List<ReservationRes> data = page.getContent().stream()
                .map(ReservationRes::from)
                .toList();

        return PaginatedDto.<ReservationRes>builder()
                .data(data)
                .count(data.size())
                .total((int) page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    /**
     * 로그인한 사용자의 디저트 예약 처리.
     * 비관적 락으로 재고 동시성 제어 후, 예약 가능 상태·구매 한도·재고를 순서대로 검증.
     */
    @Override
    @Transactional
    public ReservationRes reserve(CustomUserDetails userDetails, ReservationReq req) {
        // 사용자 조회
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    return new BusinessException(ErrorCode.NOT_FOUND, "유저");
                });

        // 디저트 조회 - 비관적 락(SELECT FOR UPDATE)으로 동시 예약 시 재고 정합성 보장
        Dessert dessert = dessertRepository.findByIdWithPessimisticLock(req.dessertId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "디저트"));

        // 예약 검증
        reservationValidator.validateReservationAvailability(dessert);
        reservationValidator.validatePurchaseLimit(user, dessert, req.count());
        reservationValidator.validateInventory(dessert, req.count());

        // 재고 차감
        dessert.decreaseInventory(req.count());

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .dessert(dessert)
                .count(req.count())
                .totalPrice(dessert.getPrice().multiply(BigDecimal.valueOf(req.count())))
                .reserveStatus(ReserveStatus.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationRes.from(savedReservation);
    }
}
