package com.ticketing.repository;

import com.ticketing.entity.Dessert;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DessertRepository extends JpaRepository<Dessert, Long> {
    List<Dessert> findAllByStoreId(Long storeId);

    /**
     * 재고 동시성 제어를 위해 비관적 락(SELECT ... FOR UPDATE)을 적용한 디저트 단건 조회.
     * 동일 디저트에 대한 동시 예약 요청 시 순차 처리를 보장.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM desserts d WHERE d.id = :id")
    Optional<Dessert> findByIdWithPessimisticLock(@Param("id") Long id);
}
