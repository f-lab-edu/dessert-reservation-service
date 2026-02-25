package com.ticketing.service;

import com.ticketing.dto.DessertReq;
import com.ticketing.dto.DessertRes;

public interface DessertService {
    DessertRes createDessert(DessertReq dessertReq);
}
