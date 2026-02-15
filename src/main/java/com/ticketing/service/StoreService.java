package com.ticketing.service;

import com.ticketing.dto.DessertRes;
import com.ticketing.dto.StoreRes;

import java.util.List;

public interface StoreService {
    List<StoreRes> getStoreList(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude);

    List<DessertRes> getStoreDesserts(Long storeId);
}
