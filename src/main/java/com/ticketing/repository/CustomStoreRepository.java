package com.ticketing.repository;

import com.ticketing.dto.StoreRes;

import java.util.List;

public interface CustomStoreRepository {
    List<StoreRes> findAllByLatitudeBetweenAndLongitudeBetween(
            double minLatitude, double maxLatitude,
            double minLongitude, double maxLongitude
    );
}
