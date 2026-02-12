package com.ticketing.repository;

import com.ticketing.entity.Dessert;
import com.ticketing.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DessertRepository extends JpaRepository<Dessert, Long> {
    @Query("SELECT COALESCE(SUM(d.inventory), 0) FROM desserts d WHERE d.store = :store")
    int sumInventoryByStore(@Param("store") Store store);
}
