# Scenario 5: Concurrency Accuracy Verification Test

## 목적
비관적 락(Pessimistic Locking)이 실제로 동시성 문제를 방지하고 과매도(Overselling)를 막는지 검증합니다.

## 테스트 시나리오

### 사전 준비 (필수)
1. **데이터베이스에 테스트 데이터 준비**:
   - 재고 10개의 디저트 생성
   - 테스트용 사용자 계정 생성

2. **환경변수 설정**:
   ```bash
   DESSERT_ID=<디저트 ID>
   USERNAME=<사용자 이메일>
   PASSWORD=<사용자 비밀번호>
   ```

### 테스트 진행
- **500명의 VU**가 동시에 예약 시도
- 각 VU는 **2개씩** 예약 시도 (총 1000개 요청, 하지만 재고는 10개)
- **예상 결과**:
  - 성공: 정확히 **5건** (5 × 2 = 10개 소진)
  - 실패: **495건** (재고 부족)
  - 과매도: **0건**

## 실행 방법

### 1. 기본 실행
```bash
cd performance-tests/scenarios

k6 run \
  -e DESSERT_ID=1 \
  -e USERNAME=user@example.com \
  -e PASSWORD=password123 \
  scenario5-concurrency-accuracy.js
```

### 2. 결과 파일 저장
```bash
k6 run \
  -e DESSERT_ID=1 \
  -e USERNAME=user@example.com \
  -e PASSWORD=password123 \
  --out json=../results/$(date +%Y-%m-%d)/scenario5_$(date +%H-%M-%S).json \
  scenario5-concurrency-accuracy.js
```

## 측정 메트릭

### Custom Counters
| 메트릭 | 설명 | 예상 값 |
|--------|------|---------|
| `reservation_success` | 성공한 예약 수 | 5 |
| `reservation_failure` | 실패한 예약 수 | 495 |
| `http_409_conflict` | HTTP 409 (재고 부족) | 대부분 |
| `http_400_bad_request` | HTTP 400 (기타 에러) | 0 또는 소수 |
| `http_other_errors` | 기타 에러 | 0 |

### Standard Metrics
- `http_req_duration`: 요청 응답 시간 (p95 < 3s, p99 < 5s)
- `http_req_failed`: 실패율 (99% 미만 - 대부분 실패가 예상됨)

## 성공 기준

### ✓ PASS 조건
1. 성공한 예약: **정확히 5건**
2. 실패한 예약: **495건**
3. 과매도 발생: **없음**

### ⚠️ FAIL 조건
- 성공 건수가 5건을 초과 (과매도 발생)
- 성공 건수가 5건 미만 (락 문제 또는 기타 에러)

## 출력 예시

```
╔═══════════════════════════════════════════════════════════════╗
║   Concurrency Accuracy Verification Results                  ║
╚═══════════════════════════════════════════════════════════════╝

  Total Attempts:           500
  Successful Reservations:  5 (Expected: 5)
  Failed Reservations:      495 (Expected: 495)

  Failure Breakdown:
    - HTTP 409 (Conflict):   495
    - HTTP 400 (Bad Req):    0
    - Other Errors:          0

  ─────────────────────────────────────────────────────────────
  Overselling Detected:     ✓ NO (PASS)
  Accuracy Test Result:     ✓ PASS
  ─────────────────────────────────────────────────────────────

  ✓ SUCCESS: Pessimistic locking working correctly!
     Exactly 5 reservations succeeded, no overselling occurred.

═══════════════════════════════════════════════════════════════
```

## 문제 해결

### 로그인 실패
- USERNAME과 PASSWORD 환경변수가 올바르게 설정되었는지 확인
- 사용자 계정이 데이터베이스에 존재하는지 확인
- soft delete 되지 않은 활성 계정인지 확인

### 모든 요청이 실패 (성공 0건)
- DESSERT_ID가 올바른지 확인
- 디저트의 재고(inventory)가 10개인지 확인
- 디저트의 openStatus가 'OPEN'인지 확인
- 애플리케이션이 정상 실행 중인지 확인 (http://localhost:80)

### 과매도 발생 (성공 5건 초과)
- **비관적 락이 제대로 작동하지 않음**
- ReservationServiceImpl의 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 확인
- 트랜잭션 격리 수준 확인
- 데이터베이스 락 로그 확인

## 기술적 세부사항

### Executor: shared-iterations
- 500 VUs가 총 500 iterations를 공유
- 각 VU가 정확히 1번씩 실행
- 동시 시작으로 최대 동시성 달성

### Authentication Flow
1. Form Login으로 세션 획득 (`/login`)
2. JSESSIONID 쿠키 추출
3. 예약 요청 시 쿠키 포함 (`/api/reservations`)

### Expected Behavior
1. 첫 5개 요청: 재고 소진까지 성공 (200/201)
2. 나머지 495개: 재고 부족으로 실패 (409 Conflict)
3. 비관적 락으로 인한 순차 처리 보장

## 참고
- 이 테스트는 성능 테스트가 아닌 **기능 정확성 검증** 테스트입니다
- 응답 시간보다 결과의 정확성이 중요합니다
- 각 테스트 실행 후 **반드시 디저트 재고를 10개로 초기화**해야 합니다
