package com.ticketing.repository;

import com.ticketing.entity.Dessert;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface DessertRepository extends JpaRepository<Dessert, Long>, CustomDessertRepository {
    List<Dessert> findAllByStoreId(Long storeId);

    /**
     * 재고 동시성 제어를 위해 비관적 락(SELECT ... FOR UPDATE)을 적용한 디저트 단건 조회.
     * 동일 디저트에 대한 동시 예약 요청 시 순차 처리를 보장.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM desserts d WHERE d.id = :id")
    Optional<Dessert> findByIdWithPessimisticLock(@Param("id") Long id);

    /**
     * PENDING 상태의 디저트 중 오픈 시각이 도래한 것을 OPEN 상태로 변경.
     */
    @Modifying
    @Query(value = "UPDATE desserts SET open_status = 'OPEN' " +
                   "WHERE open_status = 'PENDING' " +
                   "AND DATE_FORMAT(open_dt, '%Y-%m-%d %H:%i') <= DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i')",
           nativeQuery = true)
    int updatePendingToOpenByOpenDt();
}
