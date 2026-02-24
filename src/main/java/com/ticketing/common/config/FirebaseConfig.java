package com.ticketing.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK 초기화 설정.
 * 서비스 계정 키 파일을 사용하여 FirebaseApp을 초기화.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-key}")
    private Resource serviceAccountKey;

    @Bean
    public FirebaseApp firebaseApp() {
        try (InputStream serviceAccount = serviceAccountKey.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase Admin SDK 초기화 완료");
                return FirebaseApp.initializeApp(options);
            } else {
                log.info("Firebase Admin SDK 이미 초기화됨");
            }
        } catch (IOException e) {
            log.error("Firebase Admin SDK 초기화 실패", e);
            throw new IllegalStateException("Firebase 초기화에 실패했습니다.", e);
        }
        return null;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
