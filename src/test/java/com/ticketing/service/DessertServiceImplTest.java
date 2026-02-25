package com.ticketing.service;

import com.ticketing.common.util.StoreSubscriberNotifier;
import com.ticketing.dto.DessertReq;
import com.ticketing.dto.DessertRes;
import com.ticketing.entity.Dessert;
import com.ticketing.entity.Store;
import com.ticketing.enums.NotificationKey;
import com.ticketing.enums.OpenStatus;
import com.ticketing.exception.BusinessException;
import com.ticketing.exception.ErrorCode;
import com.ticketing.repository.DessertRepository;
import com.ticketing.repository.StoreRepository;
import com.ticketing.service.impl.DessertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DessertServiceImplTest {
    @InjectMocks
    private DessertServiceImpl dessertService;
    @Mock
    private DessertRepository dessertRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreSubscriberNotifier storeSubscriberNotifier;

    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .id(1L)
                .name("스위트 디저트")
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
    }

    private void mockStoreRepository() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
    }

    @Test
    @DisplayName("디저트 생성 성공 - openDt가 미래인 경우 openStatus는 PENDING")
    void createDessertWithFutureOpenDt() {
        // given
        mockStoreRepository();
        LocalDateTime futureOpenDt = LocalDateTime.now().plusHours(2);
        DessertReq req = new DessertReq(
                1L,
                "딸기 쇼트케이크",
                BigDecimal.valueOf(8500),
                10,
                5,
                futureOpenDt
        );

        Dessert savedDessert = Dessert.builder()
                .id(1L)
                .store(store)
                .name("딸기 쇼트케이크")
                .price(BigDecimal.valueOf(8500))
                .inventory(10)
                .purchaseLimit(5)
                .openDt(futureOpenDt)
                .openStatus(OpenStatus.PENDING)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class))).thenReturn(0);

        // when
        DessertRes result = dessertService.createDessert(req);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getDessertId());
        assertEquals("딸기 쇼트케이크", result.getName());
        assertEquals(0, BigDecimal.valueOf(8500).compareTo(result.getPrice()));
        assertEquals(10, result.getInventory());
        assertEquals(5, result.getPurchaseLimit());
        assertEquals(OpenStatus.PENDING, result.getOpenStatus());

        // Dessert 저장 검증
        ArgumentCaptor<Dessert> dessertCaptor = ArgumentCaptor.forClass(Dessert.class);
        verify(dessertRepository).save(dessertCaptor.capture());
        Dessert capturedDessert = dessertCaptor.getValue();
        assertEquals(OpenStatus.PENDING, capturedDessert.getOpenStatus());
        assertEquals(store, capturedDessert.getStore());

        // 알림 발송 검증
        verify(storeSubscriberNotifier).notifySubscribers(
                eq(1L),
                eq(NotificationKey.DESSERT_UPLOAD)
        );
    }

    @Test
    @DisplayName("디저트 생성 성공 - openDt가 과거인 경우 openStatus는 OPEN")
    void createDessertWithPastOpenDt() {
        // given
        mockStoreRepository();
        LocalDateTime pastOpenDt = LocalDateTime.now().minusHours(1);
        DessertReq req = new DessertReq(
                1L,
                "초코 마카롱",
                BigDecimal.valueOf(3500),
                20,
                10,
                pastOpenDt
        );

        Dessert savedDessert = Dessert.builder()
                .id(2L)
                .store(store)
                .name("초코 마카롱")
                .price(BigDecimal.valueOf(3500))
                .inventory(20)
                .purchaseLimit(10)
                .openDt(pastOpenDt)
                .openStatus(OpenStatus.OPEN)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class))).thenReturn(5);

        // when
        DessertRes result = dessertService.createDessert(req);

        // then
        assertNotNull(result);
        assertEquals(OpenStatus.OPEN, result.getOpenStatus());

        // Dessert 저장 검증
        ArgumentCaptor<Dessert> dessertCaptor = ArgumentCaptor.forClass(Dessert.class);
        verify(dessertRepository).save(dessertCaptor.capture());
        Dessert capturedDessert = dessertCaptor.getValue();
        assertEquals(OpenStatus.OPEN, capturedDessert.getOpenStatus());

        // 알림 발송 검증
        verify(storeSubscriberNotifier, times(1)).notifySubscribers(anyLong(), any(NotificationKey.class));
    }

    @Test
    @DisplayName("디저트 생성 성공 - openDt가 현재 시각과 같은 경우 openStatus는 OPEN")
    void createDessertWithCurrentOpenDt() {
        // given
        mockStoreRepository();
        LocalDateTime now = LocalDateTime.now();
        DessertReq req = new DessertReq(
                1L,
                "치즈케이크",
                BigDecimal.valueOf(6000),
                15,
                3,
                now
        );

        Dessert savedDessert = Dessert.builder()
                .id(3L)
                .store(store)
                .name("치즈케이크")
                .price(BigDecimal.valueOf(6000))
                .inventory(15)
                .purchaseLimit(3)
                .openDt(now)
                .openStatus(OpenStatus.OPEN)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class))).thenReturn(0);

        // when
        DessertRes result = dessertService.createDessert(req);

        // then
        assertEquals(OpenStatus.OPEN, result.getOpenStatus());

        // Dessert 저장 시 openStatus가 OPEN으로 설정되었는지 검증
        ArgumentCaptor<Dessert> dessertCaptor = ArgumentCaptor.forClass(Dessert.class);
        verify(dessertRepository).save(dessertCaptor.capture());
        assertEquals(OpenStatus.OPEN, dessertCaptor.getValue().getOpenStatus());
    }

    @Test
    @DisplayName("존재하지 않는 스토어 ID로 디저트 생성 시 예외 발생")
    void throwsExceptionWhenStoreNotFound() {
        // given
        LocalDateTime futureOpenDt = LocalDateTime.now().plusHours(1);
        DessertReq req = new DessertReq(
                999L,
                "딸기 쇼트케이크",
                BigDecimal.valueOf(8500),
                10,
                5,
                futureOpenDt
        );

        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> dessertService.createDessert(req)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("스토어"));

        // Repository 상호작용 검증
        verify(storeRepository).findById(999L);
        verify(dessertRepository, never()).save(any());
        verify(storeSubscriberNotifier, never()).notifySubscribers(anyLong(), any(NotificationKey.class));
    }

    @Test
    @DisplayName("모든 필드가 올바르게 매핑되는지 검증")
    void verifyAllFieldsAreMappedCorrectly() {
        // given
        mockStoreRepository();
        LocalDateTime openDt = LocalDateTime.now().plusMinutes(30);
        BigDecimal price = BigDecimal.valueOf(12500);
        DessertReq req = new DessertReq(
                1L,
                "프리미엄 케이크",
                price,
                50,
                20,
                openDt
        );

        Dessert savedDessert = Dessert.builder()
                .id(10L)
                .store(store)
                .name("프리미엄 케이크")
                .price(price)
                .inventory(50)
                .purchaseLimit(20)
                .openDt(openDt)
                .openStatus(OpenStatus.PENDING)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class))).thenReturn(0);

        // when
        DessertRes result = dessertService.createDessert(req);

        // then - 모든 필드 검증
        assertEquals(10L, result.getDessertId());
        assertEquals("프리미엄 케이크", result.getName());
        assertEquals(0, price.compareTo(result.getPrice()));
        assertEquals(50, result.getInventory());
        assertEquals(20, result.getPurchaseLimit());
        assertEquals(openDt, result.getOpenDt());
        assertEquals(OpenStatus.PENDING, result.getOpenStatus());

        // ArgumentCaptor로 save 메서드에 전달된 Dessert 객체 검증
        ArgumentCaptor<Dessert> captor = ArgumentCaptor.forClass(Dessert.class);
        verify(dessertRepository).save(captor.capture());
        Dessert capturedDessert = captor.getValue();

        assertEquals(store, capturedDessert.getStore());
        assertEquals("프리미엄 케이크", capturedDessert.getName());
        assertEquals(0, price.compareTo(capturedDessert.getPrice()));
        assertEquals(50, capturedDessert.getInventory());
        assertEquals(20, capturedDessert.getPurchaseLimit());
        assertEquals(openDt, capturedDessert.getOpenDt());
        assertEquals(OpenStatus.PENDING, capturedDessert.getOpenStatus());
    }

    @Test
    @DisplayName("알림 발송이 올바른 파라미터로 호출되는지 검증")
    void verifyNotificationIsSentWithCorrectParameters() {
        // given
        mockStoreRepository();
        LocalDateTime futureOpenDt = LocalDateTime.now().plusHours(3);
        DessertReq req = new DessertReq(
                1L,
                "레몬 타르트",
                BigDecimal.valueOf(7000),
                8,
                4,
                futureOpenDt
        );

        Dessert savedDessert = Dessert.builder()
                .id(5L)
                .store(store)
                .name("레몬 타르트")
                .price(BigDecimal.valueOf(7000))
                .inventory(8)
                .purchaseLimit(4)
                .openDt(futureOpenDt)
                .openStatus(OpenStatus.PENDING)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class))).thenReturn(10);

        // when
        dessertService.createDessert(req);

        // then - 알림 발송 파라미터 검증
        verify(storeSubscriberNotifier).notifySubscribers(
                eq(1L),
                eq(NotificationKey.DESSERT_UPLOAD)
        );
    }

    @Test
    @DisplayName("알림 발송 실패해도 디저트 생성은 성공")
    void createDessertSuccessEvenIfNotificationFails() {
        // given
        mockStoreRepository();
        LocalDateTime futureOpenDt = LocalDateTime.now().plusHours(1);
        DessertReq req = new DessertReq(
                1L,
                "티라미수",
                BigDecimal.valueOf(9000),
                12,
                6,
                futureOpenDt
        );

        Dessert savedDessert = Dessert.builder()
                .id(100L)
                .store(store)
                .name("티라미수")
                .price(BigDecimal.valueOf(9000))
                .inventory(12)
                .purchaseLimit(6)
                .openDt(futureOpenDt)
                .openStatus(OpenStatus.PENDING)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        // 알림 발송 시 예외 발생
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "알림 템플릿"));

        // when
        DessertRes result = dessertService.createDessert(req);

        // then - 디저트는 정상 생성됨
        assertNotNull(result);
        assertEquals(100L, result.getDessertId());
        assertEquals("티라미수", result.getName());
        assertEquals(OpenStatus.PENDING, result.getOpenStatus());

        // 디저트 저장 확인
        verify(dessertRepository).save(any(Dessert.class));

        // 알림 발송 시도는 했지만 실패함
        verify(storeSubscriberNotifier).notifySubscribers(
                eq(1L),
                eq(NotificationKey.DESSERT_UPLOAD)
        );
    }

    @Test
    @DisplayName("알림 발송 중 일반 예외 발생해도 디저트 생성은 성공")
    void createDessertSuccessEvenIfNotificationThrowsRuntimeException() {
        // given
        mockStoreRepository();
        LocalDateTime pastOpenDt = LocalDateTime.now().minusHours(1);
        DessertReq req = new DessertReq(
                1L,
                "에클레어",
                BigDecimal.valueOf(4500),
                30,
                15,
                pastOpenDt
        );

        Dessert savedDessert = Dessert.builder()
                .id(200L)
                .store(store)
                .name("에클레어")
                .price(BigDecimal.valueOf(4500))
                .inventory(30)
                .purchaseLimit(15)
                .openDt(pastOpenDt)
                .openStatus(OpenStatus.OPEN)
                .build();

        when(dessertRepository.save(any(Dessert.class))).thenReturn(savedDessert);
        // 알림 발송 시 일반 예외 발생
        when(storeSubscriberNotifier.notifySubscribers(anyLong(), any(NotificationKey.class)))
                .thenThrow(new RuntimeException("Redis 연결 실패"));

        // when
        DessertRes result = dessertService.createDessert(req);

        // then - 디저트는 정상 생성됨
        assertNotNull(result);
        assertEquals(200L, result.getDessertId());
        assertEquals("에클레어", result.getName());
        assertEquals(OpenStatus.OPEN, result.getOpenStatus());

        // 디저트 저장 확인
        verify(dessertRepository).save(any(Dessert.class));

        // 알림 발송 시도 확인
        verify(storeSubscriberNotifier).notifySubscribers(anyLong(), any(NotificationKey.class));
    }
}
