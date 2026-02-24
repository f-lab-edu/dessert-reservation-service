package com.ticketing.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.entity.QDessert;
import com.ticketing.enums.OpenStatus;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class DessertRepositoryImpl implements CustomDessertRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 시간 범위에 오픈 예정인 디저트의 매장 ID 중복 제거 후 조회.
     * open_dt 컬럼의 인덱스를 활용하여 효율적인 범위 검색 수행.
     */
    @Override
    public List<Long> findDistinctStoreIdsByOpenDtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        QDessert dessert = QDessert.dessert;

        return queryFactory
                .select(dessert.store.id)
                .distinct()
                .from(dessert)
                .where(
                        dessert.openStatus.eq(OpenStatus.PENDING),
                        dessert.openDt.goe(startTime),  // greater or equal
                        dessert.openDt.lt(endTime)      // less than
                )
                .fetch();
    }
}
