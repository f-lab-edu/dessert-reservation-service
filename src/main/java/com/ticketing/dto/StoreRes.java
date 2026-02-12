package com.ticketing.dto;

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
