package com.ticketing.service.impl;

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
import com.ticketing.service.DessertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DessertServiceImpl implements DessertService {
    private final DessertRepository dessertRepository;
    private final StoreRepository storeRepository;
    private final StoreSubscriberNotifier storeSubscriberNotifier;

    /**
     * 디저트 생성.
     * openDt가 현재 시각 이전이거나 같으면 openStatus를 OPEN으로, 이후이면 PENDING으로 설정.
     * 생성 후 해당 스토어 구독자에게 신규 디저트 등록 알림 발송.
     */
    @Override
    @Transactional
    public DessertRes createDessert(DessertReq dessertReq) {
        Store store = storeRepository.findById(dessertReq.storeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스토어"));

        // openDt 기준으로 openStatus 결정
        LocalDateTime now = LocalDateTime.now();
        OpenStatus openStatus = dessertReq.openDt().isAfter(now) ? OpenStatus.PENDING : OpenStatus.OPEN;

        Dessert dessert = Dessert.builder()
                .store(store)
                .name(dessertReq.name())
                .price(dessertReq.price())
                .inventory(dessertReq.inventory())
                .purchaseLimit(dessertReq.purchaseLimit())
                .openDt(dessertReq.openDt())
                .openStatus(openStatus)
                .build();

        DessertRes result = DessertRes.from(dessertRepository.save(dessert));

        // 구독자에게 신규 디저트 등록 알림 발송
        // 알림 발송 실패해도 디저트 생성은 성공 처리
        try {
            storeSubscriberNotifier.notifySubscribers(
                    store.getId(),
                    NotificationKey.DESSERT_UPLOAD
            );
        } catch (Exception e) {
            log.error("디저트 등록 알림 발송 실패. 디저트 생성은 완료됨. dessertId={}, storeId={}",
                    result.getDessertId(), store.getId(), e);
        }

        return result;
    }
}
