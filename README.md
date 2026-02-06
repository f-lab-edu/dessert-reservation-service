# 🍰 Jjoncketing - 디저트 예약 티켓팅 서비스

> 인기 디저트 오픈런을 대비한 대규모 트래픽 처리 및 성능 최적화 프로젝트

## 📋 목차
- [프로젝트 소개](#-프로젝트-소개)
- [프로젝트 목표](#-프로젝트-목표)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [프로젝트 구조](#-프로젝트-구조)
- [성능 최적화 전략](#-성능-최적화-전략)
- [시작하기](#-시작하기)
- [API 명세](#-api-명세)
- [성능 테스트](#-성능-테스트)

---

## 🎯 프로젝트 소개

**Jjoncketing**은 인기 디저트를 지도에서 검색하고 예약할 수 있는 티켓팅 서비스입니다.
특히 오픈 시간에 발생하는 **오픈런(대규모 동시 접속)** 상황을 가정하여,
대용량 트래픽 처리와 성능 최적화에 중점을 둔 학습 프로젝트입니다.

### 배경
- 인기 디저트 매장의 경우 오픈 시간에 수천~수만 명의 사용자가 동시 접속
- 선착순 예약 시스템에서 발생하는 동시성 문제 해결 필요
- 데이터베이스 부하 분산 및 응답 속도 최적화 필요

---

## 🎯 프로젝트 목표

1. **대규모 트래픽 처리**
   - 동시 접속자 수천 명 이상 처리 가능한 시스템 구축
   - 오픈런 시나리오에서의 안정적인 서비스 제공

2. **동시성 제어**
   - 재고 관리 시스템에서 발생하는 Race Condition 해결
   - 분산 락(Distributed Lock)을 활용한 데이터 정합성 보장

3. **성능 최적화**
   - 응답 시간 최소화 (목표: P95 < 500ms)
   - 데이터베이스 쿼리 최적화 및 캐싱 전략 적용
   - Redis를 활용한 읽기 성능 향상
---

## 🚀 주요 기능

### 1. 인기 디저트 재고 조회
- 실시간 재고 현황 조회
- 지도 기반 매장 검색 (예정)
- 카테고리별 디저트 필터링
- Redis 캐싱을 통한 빠른 조회 성능

### 2. 디저트 예약 시스템
- 오픈 시간 기반 예약 시작 알림 (Firebase FCM)
- 선착순 예약 처리
- 재고 실시간 차감

### 3. 동시성 제어
- 분산 락을 통한 재고 관리
- 낙관적 락 / 비관적 락 전략 비교
- Redis를 활용한 대기열 시스템 (예정)

---

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Gradle 8.14
- MySQL 8.0
- Redis 7.2
  - redis-cache: 데이터 캐싱 (LRU 정책)
  - redis-queue: 메시지 큐 / 대기열 관리 (AOF 영속성)
- Docker, Docker Compose
- Firebase Cloud Messaging (FCM)
- k6

---

## 🏗 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│                   Spring Boot Application                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Controller   │  │  Service     │  │  Repository  │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└───────────┬────────────────────┬────────────────────────────┘
            │                    │
            ▼                    ▼
┌───────────────────┐   ┌───────────────────────────────────┐
│   Redis Cluster   │   │        MySQL Database             │
│                   │   │                                   │
│ ┌───────────────┐ │   │  ┌─────────────────────────────┐  │
│ │ redis-cache   │ │   │  │  Dessert, Store, Order      │  │
│ │  (캐싱 전용)    │  │  │  │  Reservation, User          │   │
│ └───────────────┘ │   │  └─────────────────────────────┘  │
│                   │   │                                   │
│ ┌───────────────┐ │   └───────────────────────────────────┘
│ │ redis-queue   │ │
│ │ (메시지 큐)     │ │
│ └───────────────┘ │
└───────────────────┘
            │
            ▼
┌───────────────────────────────────────┐
│      Firebase Cloud Messaging         │
│       (Push Notification)             │
└───────────────────────────────────────┘
```

### 아키텍처 특징

1. **레이어드 아키텍처**
   - Controller: HTTP 요청 처리 및 검증
   - Service: 비즈니스 로직 처리
   - Repository: 데이터 액세스 계층

2. **캐싱 전략**
   - Look-Aside 패턴 적용
   - Redis Cache를 통한 읽기 성능 최적화
   - TTL 기반 캐시 무효화

3. **데이터 영속성**
   - MySQL: 트랜잭션 데이터 저장
   - Redis Queue: AOF(Append Only File) 방식으로 메시지 영속성 보장

---

## 📁 프로젝트 구조

```
jjoncketing/
├── src/
│   ├── main/
│   │   ├── java/com/ticketing/
│   │   │   ├── common/            # 공통 유틸리티, 상수, 예외 처리
│   │   │   ├── controller/        # REST API 엔드포인트
│   │   │   ├── service/           # 비즈니스 로직
│   │   │   ├── repository/        # 데이터 액세스 계층
│   │   │   ├── entity/            # JPA 엔티티
│   │   │   ├── dto/               # 데이터 전송 객체
│   │   │   └── JjoncketingApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-docker.properties
│   └── test/
│       └── java/                  # 단위/통합 테스트
├── performance-test/              # 성능 테스트 스크립트 (k6)
├── scripts/                       # 유틸리티 스크립트
│   └── performance-test.sh
├── config/                        # 설정 파일
│   └── firebase-service-account.json
├── init-db/                       # 데이터베이스 초기화 스크립트
├── Dockerfile                     # 멀티 스테이지 빌드
├── docker-compose.yaml            # 로컬 개발 환경
├── build.gradle                   # Gradle 빌드 설정
└── README.md
```

---

## ⚡ 성능 최적화 전략

(내용 추가 예정)

---

## 🚀 시작하기

### 사전 요구사항
- Java 17 이상
- Docker & Docker Compose
- Gradle 8.x (또는 Gradle Wrapper 사용)

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-username/jjoncketing.git
cd jjoncketing
```

### 2. 환경 변수 설정
`.env` 파일을 생성하고 다음 내용을 작성합니다:
```agsl
# app
PUBLISHED_APP

# Database
DB_NAME
DB_USERNAME
DB_PASSWORD
DB_PORT
DB_HOST
DB_URL
DB_DATA_PATH

# Redis
REDIS_CACHE_PORT
REDIS_CACHE_PASSWORD
REDIS_CACHE_DATA_PATH

REDIS_QUEUE_PORT
REDIS_QUEUE_PASSWORD
REDIS_QUEUE_DATA_PATH

# Firebase
FIREBASE_CONFIG_PATH
FIREBASE_KEY_PATH
```

### 3. Docker Compose로 실행

#### build.gradle 수정 (Plain JAR 비활성화)
먼저 `build.gradle` 파일에 다음 내용을 추가하세요:

```gradle
tasks.named('jar') {
    enabled = false
}
```

#### 컨테이너 실행
```bash
# 컨테이너 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d

# 로그 확인
docker logs -f backend
```

### 4. 애플리케이션 확인
```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# 컨테이너 상태 확인
docker ps
```

### 5. 종료
```bash
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```
