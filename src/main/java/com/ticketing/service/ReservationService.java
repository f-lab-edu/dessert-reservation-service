package com.ticketing.service;

import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;

public interface ReservationService {
    ReservationRes reserve(CustomUserDetails userDetails, ReservationReq req);
}
