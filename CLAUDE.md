# jjoncketing 프로젝트

디저트 예약 서비스. 사용자가 지도에서 주변 스토어를 탐색하고 디저트를 예약할 수 있다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.6 |
| ORM | Spring Data JPA + QueryDSL 5.0.0 |
| DB | MySQL 8.0 |
| Queue | Redis |
| Push | Firebase Admin SDK (FCM) |
| Build | Gradle 8.10.2 |
| Infra | Docker Compose |

## 프로젝트 구조

```
src/main/java/com/ticketing/
├── common/
│   ├── config/
│   │   └── QueryDslConfig.java         # JPAQueryFactory 빈 등록
│   ├── controller/
│   │   └── BaseController.java         # 공통 /api prefix, CORS 설정
│   ├── dto/
│   │   └── PaginatedDto.java           # 페이지네이션 공통 DTO
│   ├── entity/
│   │   └── BaseEntity.java             # 공통 엔티티 (createdDt 관리)
│   ├── exception/
│   │   ├── ErrorResponse.java          # 에러 응답 표준 DTO
│   │   └── GlobalExceptionHandler.java # 전역 예외 처리 핸들러
│   ├── security/
│   │   ├── CustomUserDetails.java      # Spring Security UserDetails 구현
│   │   └── SecurityConfig.java         # 인증/인가 설정
│   └── validation/
│       ├── RangeValidator.java         # 범위 검증 로직
│       └── ReservationValidator.java   # 예약 검증 로직
├── controller/
│   ├── ReservationController.java      # 예약 API
│   ├── StoreController.java            # 스토어 API
│   └── SubscriptionController.java     # 구독 API
├── dto/
│   ├── DessertRes.java                 # 디저트 응답 DTO
│   ├── ReservationReq.java             # 예약 요청 DTO
│   ├── ReservationRes.java             # 예약 응답 DTO
│   ├── StoreRequest.java               # 스토어 요청 DTO
│   └── StoreRes.java                   # 스토어 응답 DTO (@QueryProjection)
├── entity/
│   ├── Dessert.java                    # BaseEntity 상속
│   ├── Reservation.java                # BaseEntity 상속
│   ├── Store.java                      # BaseEntity 상속
│   ├── Subscription.java               # 복합키 엔티티
│   ├── SubscriptionPk.java             # @IdClass 복합키 클래스
│   └── User.java                       # BaseEntity 상속
├── enums/
│   ├── OpenStatus.java                 # 디저트 오픈 상태 (PENDING, OPEN, CLOSED)
│   └── ReserveStatus.java              # 예약 상태 (PENDING, CONFIRMED, CANCELLED)
├── exception/
│   ├── BusinessException.java          # 비즈니스 로직 예외 클래스
│   └── ErrorCode.java                  # 에러 코드 정의 Enum
├── repository/
│   ├── CustomStoreRepository.java      # QueryDSL 커스텀 메서드 인터페이스
│   ├── DessertRepository.java
│   ├── ReservationRepository.java
│   ├── StoreRepository.java            # JpaRepository + CustomStoreRepository
│   ├── SubscriptionRepository.java
│   ├── UserRepository.java
│   └── impl/
│       └── StoreRepositoryImpl.java    # QueryDSL 구현체
├── scheduler/
│   └── DessertOpenScheduler.java       # 디저트 오픈 상태 자동 변경 (15분 주기)
└── service/
    ├── ReservationService.java
    ├── StoreService.java
    ├── SubscriptionService.java
    └── impl/
        ├── ReservationServiceImpl.java
        ├── StoreServiceImpl.java
        └── SubscriptionServiceImpl.java
```

## 도메인 모델

```
User ──────────── Subscription ──────────── Store
 │                  (복합키)                  │
 │                                           │
 └──── Reservation ──────────── Dessert ─────┘
```

- **User**: 사용자. `push_token`(FCM), soft delete(`deleted_dt`) 포함. BaseEntity 상속
- **Store**: 디저트 가게. 위도/경도(`Double`)로 지도 위치 표현. BaseEntity 상속
- **Dessert**: 디저트. `inventory`(재고), `purchaseLimit`, `openStatus`, `openDt` 포함. BaseEntity 상속
- **Reservation**: User-Dessert 예약. `count`, `totalPrice`, `reserveStatus` 포함. BaseEntity 상속
- **Subscription**: User-Store 구독. `@IdClass(SubscriptionPk.class)`로 복합키 구성
- **BaseEntity**: 모든 엔티티의 공통 필드(`createdDt`) 관리. JPA Auditing 적용

## 주요 기능

### 1. 예약 시스템 (Reservation)
- 디저트 예약 생성/조회
- 재고 검증 및 구매 한도 검증 (ReservationValidator)
- 비관적 락(Pessimistic Lock)을 통한 동시성 제어
- 예약 상태 관리: PENDING, CONFIRMED, CANCELLED

### 2. 구독 시스템 (Subscription)
- 사용자가 관심 있는 스토어 구독/구독 취소
- 구독한 스토어 목록 조회
- User-Store 복합키(@IdClass) 구성

### 3. 스케줄러 (DessertOpenScheduler)
- 15분마다 실행되어 디저트 오픈 시각 자동 변경
- PENDING → OPEN 상태 전환 (openDt 기준)
- `scheduler.dessert-open.enabled` 설정으로 활성화/비활성화

### 4. 예외 처리 시스템
- **GlobalExceptionHandler**: 전역 예외 처리 (`@RestControllerAdvice`)
- **BusinessException**: 비즈니스 로직 예외. ErrorCode와 연동
- **ErrorCode**: 에러 코드 및 HTTP 상태 정의 (Enum)
- **ErrorResponse**: 표준화된 에러 응답 DTO

### 5. 인증/인가 (Spring Security)
- Form Login 기반 인증
- REST API는 인증 실패 시 401 반환 (리다이렉트 없음)
- 일반 페이지는 로그인 페이지로 리다이렉트
- Soft delete된 사용자 제외 처리
- BCrypt 비밀번호 인코딩

### 6. Validation
- **ReservationValidator**: 예약 가능 여부, 구매 한도, 재고 검증
- **RangeValidator**: 범위 검증 로직
- Bean Validation 실패 시 GlobalExceptionHandler에서 자동 처리

## 빌드 및 실행

```bash
# 테스트 포함 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test

# 로컬 실행 (application-local.properties 사용)
./gradlew bootRun --args='--spring.profiles.active=local'

# Docker 컨테이너 앱만 재빌드
docker-compose up --build app
```

## 프로파일

| 프로파일 | 설정 파일 | 용도 |
|---------|----------|------|
| (기본) | `application.properties` | Docker 컨테이너 환경 |
| `local` | `application-local.properties` | 로컬 개발 (gitignore됨) |
| `test` | `src/test/resources/application.properties` | H2 인메모리 DB 사용 |

로컬 실행 시 MySQL/Redis는 Docker 컨테이너(`localhost` 포트 포워딩)에 연결된다.

### 주요 설정
- **스케줄러 활성화**: `scheduler.dessert-open.enabled=true` (디저트 오픈 스케줄러)
- **JPA Auditing**: `@EnableJpaAuditing` 활성화 (BaseEntity의 createdDt 자동 관리)

## 인프라 (Docker Compose)

```
app    → Spring Boot 애플리케이션 (8080)
mysql  → MySQL 8.0
redis  → Redis (큐 용도. 캐싱은 현재 미사용)
```

환경 변수는 `.env` 파일로 관리 (gitignore됨).

## QueryDSL 커스텀 Repository 패턴

복잡한 동적 쿼리는 `Custom` 인터페이스 + `Impl` 구현체로 분리한다.

```
StoreRepository extends JpaRepository<Store, Long>, CustomStoreRepository
StoreRepositoryImpl implements CustomStoreRepository  ← QueryDSL 실제 구현
```

Q클래스는 `./gradlew compileJava` 실행 시 `build/generated`에 자동 생성된다.

DTO Projection은 `@QueryProjection` 생성자를 사용한다 (`QStoreRes` 자동 생성).

## 코드 컨벤션

### 엔티티
- Lombok: `@Getter`, `@Builder`, `@RequiredArgsConstructor`, `@AllArgsConstructor`
- 연관관계 기본값: `FetchType.LAZY`
- 테이블명: `@Entity(name = "...")` 사용 중 (추후 `@Table(name = "...")`으로 분리 권장)
- 공통 필드는 `BaseEntity` 상속으로 관리 (`createdDt`)

### 서비스
- 클래스 레벨에 `@Transactional(readOnly = true)` 적용
- 쓰기 메서드에는 `@Transactional` 명시적으로 추가

### DTO
- 엔티티 → DTO 변환은 DTO 내 정적 팩토리 메서드 `from()` 사용
- QueryDSL Projection용 생성자에 `@QueryProjection` 추가

### 컨트롤러
- `BaseController` 상속으로 `/api` prefix 및 CORS 적용
- 응답은 `ResponseEntity<T>` 래핑
- 인증이 필요한 API는 SecurityConfig에서 설정

### 예외 처리
- 비즈니스 로직 예외는 `BusinessException`을 던지고 `ErrorCode`로 에러 정의
- 전역 예외 처리는 `GlobalExceptionHandler`에서 일괄 처리
- 에러 응답은 `ErrorResponse` 형식으로 통일

### Validation
- 도메인 로직 검증은 별도 Validator 클래스로 분리 (`ReservationValidator`, `RangeValidator`)
- Bean Validation(`@Valid`)은 컨트롤러 레벨에서 적용

### 의존성 주입
- `@RequiredArgsConstructor` + `final` 필드로 생성자 주입

### 주석
모든 메서드에 호출 맥락 또는 동작 방식을 간단히 주석으로 작성한다.

```java
// 좋은 예시
/**
 * 지도 화면에서 사용자의 화면 범위(위도/경도) 내에 있는 스토어 목록 조회.
 * 각 스토어의 위치 정보와 잔여 디저트 수량을 함께 반환.
 */
public List<StoreRes> getStoreList(...) { ... }

/**
 * Store와 Dessert를 LEFT JOIN하여 단일 쿼리로 스토어별 재고 합산.
 * N+1 문제를 방지하기 위해 QueryDSL DTO Projection으로 직접 반환.
 */
public List<StoreRes> findAllByLatitudeBetweenAndLongitudeBetween(...) { ... }
```

- 말투: `~.` 으로 끝나는 간결한 서술형 (예: "스토어 목록 조회.")
- `~합니다` 형태의 경어체 사용 금지
