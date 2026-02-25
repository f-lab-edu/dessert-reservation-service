package com.ticketing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DessertReq (
    @NotNull
    Long storeId,
    @NotNull
    String name,
    @NotNull
    @DecimalMin(value = "0", message = "가격은 음수일 수 없습니다.")
    BigDecimal price,
    @NotNull
    @Min(value = 0, message = "재고는 음수 일 수 없습니다.")
    Integer inventory,
    @NotNull
    Integer purchaseLimit,
    @NotNull
    @FutureOrPresent(message = "오픈 날짜는 오늘 이전 일 수 없습니다.")
    LocalDateTime openDt
){}
