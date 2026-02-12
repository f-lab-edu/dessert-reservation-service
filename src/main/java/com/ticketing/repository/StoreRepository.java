package com.ticketing.repository;

import com.ticketing.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findAllByLatitudeBetweenAndLongitudeBetween(
            double minLatitude, double maxLatitude,
            double minLongitude, double maxLongitude
    );
}
