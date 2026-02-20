package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.dto.StoreRes;
import com.ticketing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController extends BaseController {

    private final SubscriptionService subscriptionService;

    /**
     * 상점 구독 토글.
     * 이미 구독 중인 상점이면 구독 취소, 구독하지 않은 상점이면 새로 구독.
     * 로그인한 사용자만 호출 가능.
     */
    @PostMapping("/subscriptions")
    public ResponseEntity<Void> toggleSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long storeId
    ) {
        subscriptionService.toggleSubscription(userDetails.getId(), storeId);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인한 사용자가 구독한 상점 리스트 조회.
     * 각 상점의 기본 정보(storeId, name, latitude, longitude)를 반환.
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<List<StoreRes>> getSubscriptionList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<StoreRes> subscriptionList = subscriptionService.getSubscriptionList(userDetails.getId());
        return ResponseEntity.ok(subscriptionList);
    }
}
