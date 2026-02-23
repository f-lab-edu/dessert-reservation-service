package com.ticketing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationReq(
        @NotNull(message = "디저트 ID는 필수입니다.")
        Long dessertId,

        @NotNull(message = "예약 수량은 필수입니다.")
        @Min(value = 1, message = "예약 수량은 1개 이상이어야 합니다.")
        Integer count
) {
}
