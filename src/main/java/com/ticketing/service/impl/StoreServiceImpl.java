package com.ticketing.service.impl;

import com.ticketing.dto.StoreRes;
import com.ticketing.repository.DessertRepository;
import com.ticketing.repository.StoreRepository;
import com.ticketing.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final DessertRepository dessertRepository;

    @Override
    public List<StoreRes> getStoreList(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        return storeRepository
                .findAllByLatitudeBetweenAndLongitudeBetween(minLatitude, maxLatitude, minLongitude, maxLongitude)
                .stream()
                .map(store -> StoreRes.from(store, dessertRepository.sumInventoryByStore(store)))
                .toList();
    }
}
