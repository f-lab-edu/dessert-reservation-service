package com.ticketing.dto;

import com.ticketing.entity.Dessert;
import com.ticketing.enums.OpenStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DessertRes {
    private Long dessertId;
    private String name;
    private Float price;
    private Integer inventory;
    private Integer purchaseLimit;
    private LocalDateTime openDt;
    private OpenStatus openStatus;

    public static DessertRes from(Dessert dessert) {
        return DessertRes.builder()
                .dessertId(dessert.getId())
                .name(dessert.getName())
                .price(dessert.getPrice())
                .inventory(dessert.getInventory())
                .purchaseLimit(dessert.getPurchaseLimit())
                .openDt(dessert.getOpenDt())
                .openStatus(dessert.getOpenStatus())
                .build();
    }
}
