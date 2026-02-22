package com.ticketing.repository;

import com.ticketing.entity.Dessert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DessertRepository extends JpaRepository<Dessert, Long> {
    List<Dessert> findAllByStoreId(Long storeId);

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
