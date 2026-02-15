package com.ticketing.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.dto.QStoreRes;
import com.ticketing.dto.StoreRes;
import com.ticketing.entity.QDessert;
import com.ticketing.entity.QStore;
import com.ticketing.repository.CustomStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements CustomStoreRepository {

    private final JPAQueryFactory queryFactory;

    private static final QStore store = QStore.store;
    private static final QDessert dessert = QDessert.dessert;

    @Override
    public List<StoreRes> findAllByLatitudeBetweenAndLongitudeBetween(
            double minLatitude, double maxLatitude,
            double minLongitude, double maxLongitude
    ) {
        return queryFactory
                .select(new QStoreRes(
                        store.id,
                        store.name,
                        store.latitude,
                        store.longitude,
                        dessert.inventory.sum().intValue()
                ))
                .from(store)
                .leftJoin(dessert).on(dessert.store.eq(store))
                .where(
                        store.latitude.between(minLatitude, maxLatitude),
                        store.longitude.between(minLongitude, maxLongitude)
                )
                .groupBy(store.id)
                .fetch();
    }
}
