package com.ticketing.service;

import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.ReservationReq;
import com.ticketing.dto.ReservationRes;
import com.ticketing.entity.Dessert;
import com.ticketing.entity.Reservation;
import com.ticketing.entity.User;
import com.ticketing.enums.OpenStatus;
import com.ticketing.enums.ReserveStatus;
import com.ticketing.repository.DessertRepository;
import com.ticketing.repository.ReservationRepository;
import com.ticketing.repository.UserRepository;
import com.ticketing.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {
    @InjectMocks
    private ReservationServiceImpl reservationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DessertRepository dessertRepository;
    @Mock
    private ReservationRepository reservationRepository;

    private CustomUserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        userDetails = CustomUserDetails.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    private Dessert.DessertBuilder createDefaultDessertBuilder() {
        return Dessert.builder()
                .id(1L)
                .name("딸기 쇼트케이크")
                .price(BigDecimal.valueOf(8500))
                .inventory(5)
                .purchaseLimit(2)
                .openStatus(OpenStatus.OPEN)
                .openDt(LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("정상 예약 성공")
    void reserveSuccessfully() {
        // given
        Dessert dessert = createDefaultDessertBuilder().build();
        ReservationReq req = new ReservationReq(1L, 1);

        Reservation savedReservation = Reservation.builder()
                .id(1L)
                .user(user)
                .dessert(dessert)
                .count(1)
                .totalPrice(BigDecimal.valueOf(8500))
                .reserveStatus(ReserveStatus.CONFIRMED)
                .build();

        when(dessertRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(dessert));
        when(reservationRepository.sumTodayCountByUserIdAndDessertId(
                eq(1L), eq(1L), eq(ReserveStatus.CANCELLED), any(LocalDate.class)
        )).thenReturn(0);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // when
        ReservationRes result = reservationService.reserve(userDetails, req);

        // then
        assertEquals(1L, result.getReservationId());
        assertEquals(1L, result.getDessertId());
        assertEquals("딸기 쇼트케이크", result.getDessertName());
        assertEquals(1, result.getCount());
        assertEquals(0, BigDecimal.valueOf(8500).compareTo(result.getTotalPrice()));
        assertEquals(ReserveStatus.CONFIRMED, result.getReserveStatus());
        assertEquals(4, dessert.getInventory()); // 재고 5 → 1 차감 → 4
    }

    @Test
    @DisplayName("존재하지 않는 디저트 예약 시 예외")
    void throwsExceptionWhenDessertNotFound() {
        // given
        ReservationReq req = new ReservationReq(999L, 1);

        when(dessertRepository.findByIdWithPessimisticLock(999L)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.reserve(userDetails, req)
        );

        assertEquals("존재하지 않는 디저트입니다.", exception.getReason());
    }

    @Test
    @DisplayName("OPEN 상태가 아닌 디저트 예약 시 예외")
    void throwsExceptionWhenDessertNotOpen() {
        // given
        Dessert dessert = createDefaultDessertBuilder()
                .openStatus(OpenStatus.SOLD_OUT)
                .build();

        ReservationReq req = new ReservationReq(1L, 1);

        when(dessertRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(dessert));

        // when & then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.reserve(userDetails, req)
        );

        assertEquals("현재 예약할 수 없는 디저트입니다.", exception.getReason());
    }

    @Test
    @DisplayName("구매 한도 초과 시 예외")
    void throwsExceptionWhenPurchaseLimitExceeded() {
        // given
        Dessert dessert = createDefaultDessertBuilder().build();
        ReservationReq req = new ReservationReq(1L, 2);

        when(dessertRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(dessert));
        when(reservationRepository.sumTodayCountByUserIdAndDessertId(
                eq(1L), eq(1L), eq(ReserveStatus.CANCELLED), any(LocalDate.class)
        )).thenReturn(1);

        // when & then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.reserve(userDetails, req)
        );

        assertEquals("구매 한도를 초과했습니다. 남은 구매 가능 수량: 1개", exception.getReason());
    }

    @Test
    @DisplayName("재고 부족 시 예외")
    void throwsExceptionWhenInsufficientInventory() {
        // given
        Dessert dessert = createDefaultDessertBuilder()
                .inventory(1)
                .purchaseLimit(10)
                .build();

        ReservationReq req = new ReservationReq(1L, 2);

        when(dessertRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(dessert));
        when(reservationRepository.sumTodayCountByUserIdAndDessertId(
                eq(1L), eq(1L), eq(ReserveStatus.CANCELLED), any(LocalDate.class)
        )).thenReturn(0);

        // when & then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.reserve(userDetails, req)
        );

        assertEquals("재고가 부족합니다. 현재 재고: 1개", exception.getReason());
    }

}
