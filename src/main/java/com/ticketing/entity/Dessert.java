package com.ticketing.entity;

import com.ticketing.enums.OpenStatus;
import com.ticketing.exception.BusinessException;
import com.ticketing.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "desserts")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Dessert{
    @Id
    @Column(name = "dessert_id")
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    private String name;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer inventory;
    @Column(name = "purchase_limit", nullable = false)
    private Integer purchaseLimit;
    @Column(name = "open_dt", nullable = false)
    private LocalDateTime openDt;
    @Enumerated(EnumType.STRING)
    @Column(name = "open_status", nullable = false)
    private OpenStatus openStatus;


    /**
     * 예약 시 재고 차감. 재고 부족 시 BusinessException 발생.
     */
    public void decreaseInventory(int count) {
        if (this.inventory < count) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
        this.inventory -= count;
    }
}
