package com.ticketing.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.dto.QStoreSubscriberDto;
import com.ticketing.dto.StoreSubscriberDto;
import com.ticketing.entity.QSubscription;
import com.ticketing.entity.QUser;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * CustomSubscriptionRepository 구현체.
 * QueryDSL을 사용하여 복잡한 쿼리 수행.
 */
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements CustomSubscriptionRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 여러 매장의 구독자 정보를 단일 쿼리로 조회.
     */
    @Override
    public List<StoreSubscriberDto> findSubscribersByStoreIds(List<Long> storeIds) {
        QSubscription subscription = QSubscription.subscription;
        QUser user = QUser.user;

        return queryFactory
                .select(new QStoreSubscriberDto(
                        subscription.storeId,
                        user.id,
                        user.pushToken
                ))
                .from(subscription)
                .join(user).on(subscription.userId.eq(user.id))
                .where(
                        subscription.storeId.in(storeIds),
                        user.pushToken.isNotNull(),
                        user.deletedDt.isNull()
                )
                .fetch();
    }
}
