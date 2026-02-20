package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.common.security.CustomUserDetails;
import com.ticketing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
