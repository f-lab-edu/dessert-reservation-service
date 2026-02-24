package com.ticketing.repository.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.dto.ReservationListRequest.SortBy;
import com.ticketing.dto.ReservationListRequest.SortDirection;
import com.ticketing.entity.QDessert;
import com.ticketing.entity.QReservation;
import com.ticketing.entity.Reservation;
import com.ticketing.enums.ReserveStatus;
import com.ticketing.repository.CustomReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticketing.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements CustomReservationRepository {

    private final JPAQueryFactory queryFactory;

    private static final QReservation reservation = QReservation.reservation;
    private static final QDessert dessert = QDessert.dessert;

    /**
     * 사용자의 예약 리스트 조회. 예약 상태로 필터링하고, 예약 날짜 또는 오픈 날짜로 정렬.
     * Page 객체로 반환하여 전체 개수 정보 포함.
     */
    @Override
    public Page<Reservation> findByUserIdWithFilters(
            Long userId,
            ReserveStatus reserveStatus,
            SortBy sortBy,
            SortDirection sortDirection,
            Pageable pageable
    ) {
        // 데이터 조회
        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .join(reservation.dessert, dessert).fetchJoin()
                .join(dessert.store, store).fetchJoin()
                .where(
                        reservation.user.id.eq(userId),
                        reserveStatusEq(reserveStatus)
                )
                .orderBy(createOrderSpecifier(sortBy, sortDirection))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(
                        reservation.user.id.eq(userId),
                        reserveStatusEq(reserveStatus)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 예약 상태 필터 조건. null인 경우 필터링하지 않음.
     */
    private BooleanExpression reserveStatusEq(ReserveStatus reserveStatus) {
        return reserveStatus != null ? reservation.reserveStatus.eq(reserveStatus) : null;
    }

    /**
     * 정렬 조건 생성. sortBy와 sortDirection에 따라 OrderSpecifier 반환.
     */
    private OrderSpecifier<?> createOrderSpecifier(SortBy sortBy, SortDirection sortDirection) {
        boolean isAsc = sortDirection == SortDirection.ASC;

        return switch (sortBy) {
            case RESERVE_DATE -> isAsc ? reservation.createdDt.asc() : reservation.createdDt.desc();
            case OPEN_DATE -> isAsc ? dessert.openDt.asc() : dessert.openDt.desc();
        };
    }
}
