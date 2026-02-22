package com.ticketing.scheduler;

import com.ticketing.repository.DessertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 디저트 오픈 상태를 자동으로 업데이트하는 스케줄러.
 * 15분마다 실행되어 PENDING 상태의 디저트 중 오픈 시각이 도래한 것을 OPEN으로 변경.
 * scheduler.dessert-open.enabled 설정으로 활성화/비활성화 제어.
 */
@Component
@ConditionalOnProperty(
    value = "scheduler.dessert-open.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@RequiredArgsConstructor
@Slf4j
public class DessertOpenScheduler {

    private final DessertRepository dessertRepository;

    /**
     * 15분마다 실행되어 디저트 상태를 PENDING에서 OPEN으로 변경.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void updateDessertOpenStatus() {
        log.info("디저트 오픈 상태 업데이트 배치 시작");

        int updatedCount = dessertRepository.updatePendingToOpenByOpenDt();

        log.info("디저트 오픈 상태 업데이트 완료. 업데이트된 디저트 수: {}", updatedCount);
    }
}
