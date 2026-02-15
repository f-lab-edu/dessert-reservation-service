package com.ticketing.repository;

import com.ticketing.entity.Dessert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DessertRepository extends JpaRepository<Dessert, Long> {
    List<Dessert> findAllByStoreId(Long storeId);
}
