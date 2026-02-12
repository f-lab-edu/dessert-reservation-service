package com.ticketing.repository;

import com.ticketing.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, CustomStoreRepository {
}
