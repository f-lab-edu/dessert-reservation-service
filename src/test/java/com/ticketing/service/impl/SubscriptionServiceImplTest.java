package com.ticketing.service.impl;

import com.ticketing.entity.Subscription;
import com.ticketing.repository.StoreRepository;
import com.ticketing.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private StoreRepository storeRepository;
    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    @DisplayName("구독하지 않은 상점에 대해 호출 시 새로운 구독 생성")
    void toggleSubscription_add() {
        // given
        Long userId = 1L;
        Long storeId = 100L;

        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(subscriptionRepository.existsByUserIdAndStoreId(userId, storeId)).thenReturn(false);

        // when
        subscriptionService.toggleSubscription(userId, storeId);

        // then
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());

        Subscription saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(storeId, saved.getStoreId());
        assertNotNull(saved.getCreatedDt());

        // 삭제 메서드는 호출되지 않아야 함
        verify(subscriptionRepository, never()).deleteByUserIdAndStoreId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("이미 구독한 상점에 대해 호출 시 구독 삭제")
    void toggleSubscription_remove() {
        // given
        Long userId = 1L;
        Long storeId = 100L;

        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(subscriptionRepository.existsByUserIdAndStoreId(userId, storeId)).thenReturn(true);

        // when
        subscriptionService.toggleSubscription(userId, storeId);

        // then
        verify(subscriptionRepository).deleteByUserIdAndStoreId(userId, storeId);

        // 저장 메서드는 호출되지 않아야 함
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("존재하지 않는 storeId로 호출 시 EntityNotFoundException 발생")
    void toggleSubscription_notFound() {
        // given
        Long userId = 1L;
        Long storeId = 999L;

        when(storeRepository.existsById(storeId)).thenReturn(false);

        // when & then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.toggleSubscription(userId, storeId)
        );

        assertEquals("존재하지 않는 상점입니다.", exception.getMessage());

        // 구독 확인, 생성, 삭제 메서드는 호출되지 않아야 함
        verify(subscriptionRepository, never()).existsByUserIdAndStoreId(anyLong(), anyLong());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(subscriptionRepository, never()).deleteByUserIdAndStoreId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("같은 사용자가 여러 상점을 구독할 수 있음")
    void toggleSubscription_multiple() {
        // given
        Long userId = 1L;
        Long storeId1 = 100L;
        Long storeId2 = 200L;

        when(storeRepository.existsById(anyLong())).thenReturn(true);
        when(subscriptionRepository.existsByUserIdAndStoreId(userId, storeId1)).thenReturn(false);
        when(subscriptionRepository.existsByUserIdAndStoreId(userId, storeId2)).thenReturn(false);

        // when
        subscriptionService.toggleSubscription(userId, storeId1);
        subscriptionService.toggleSubscription(userId, storeId2);

        // then
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("구독 추가 후 다시 호출하면 구독 취소됨")
    void toggleSubscription_verify() {
        // given
        Long userId = 1L;
        Long storeId = 100L;

        when(storeRepository.existsById(storeId)).thenReturn(true);

        // 첫 번째 호출: 구독 추가
        when(subscriptionRepository.existsByUserIdAndStoreId(userId, storeId))
                .thenReturn(false);

        subscriptionService.toggleSubscription(userId, storeId);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));

        // 두 번째 호출: 구독 취소
        when(subscriptionRepository.existsByUserIdAndStoreId(userId, storeId))
                .thenReturn(true);

        subscriptionService.toggleSubscription(userId, storeId);
        verify(subscriptionRepository, times(1)).deleteByUserIdAndStoreId(userId, storeId);
    }
}
