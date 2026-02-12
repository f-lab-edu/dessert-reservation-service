package com.ticketing.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.ticketing.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreRes {
    private Long storeId;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer totalInventory;

    @QueryProjection
    public StoreRes(Long storeId, String name, Double latitude, Double longitude, Integer totalInventory) {
        this.storeId = storeId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalInventory = totalInventory;
    }

    public static StoreRes from(Store store, Integer totalInventory) {
        return StoreRes.builder()
                .storeId(store.getId())
                .name(store.getName())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .totalInventory(totalInventory)
                .build();
    }
}
