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
│   │   └── QueryDslConfig.java       # JPAQueryFactory 빈 등록
│   ├── controller/
│   │   └── BaseController.java       # 공통 /api prefix, CORS 설정
│   └── dto/
│       └── PaginatedDto.java         # 페이지네이션 공통 DTO
├── controller/
│   └── StoreController.java
├── dto/
│   └── StoreRes.java                 # @QueryProjection 생성자 포함
├── entity/
│   ├── User.java
│   ├── Store.java
│   ├── Dessert.java
│   ├── Reservation.java
│   ├── Subscription.java             # 복합키 엔티티
│   └── SubscriptionPk.java           # @IdClass 복합키 클래스
├── repository/
│   ├── StoreRepository.java          # JpaRepository + CustomStoreRepository
│   ├── CustomStoreRepository.java    # QueryDSL 커스텀 메서드 인터페이스
│   ├── StoreRepositoryImpl.java      # QueryDSL 구현체
│   └── DessertRepository.java
└── service/
    ├── StoreService.java
    └── impl/
        └── StoreServiceImpl.java
```

## 도메인 모델

```
User ──────────── Subscription ──────────── Store
 │                  (복합키)                  │
 │                                           │
 └──── Reservation ──────────── Dessert ─────┘
```

- **User**: 사용자. `push_token`(FCM), soft delete(`deleted_dt`) 포함
- **Store**: 디저트 가게. 위도/경도(`Double`)로 지도 위치 표현
- **Dessert**: 디저트. `inventory`(재고), `purchaseLimit`, `openStatus`, `openDt` 포함
- **Reservation**: User-Dessert 예약. `count`, `totalPrice`, `reserveStatus` 포함
- **Subscription**: User-Store 구독. `@IdClass(SubscriptionPk.class)`로 복합키 구성

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

### 서비스
- 클래스 레벨에 `@Transactional(readOnly = true)` 적용
- 쓰기 메서드에는 `@Transactional` 명시적으로 추가

### DTO
- 엔티티 → DTO 변환은 DTO 내 정적 팩토리 메서드 `from()` 사용
- QueryDSL Projection용 생성자에 `@QueryProjection` 추가

### 컨트롤러
- `BaseController` 상속으로 `/api` prefix 및 CORS 적용
- 응답은 `ResponseEntity<T>` 래핑

### 의존성 주입
- `@RequiredArgsConstructor` + `final` 필드로 생성자 주입
