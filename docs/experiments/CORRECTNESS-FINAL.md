# 거래 정합성 최종 검증 결과 (FIX-E)

- 브랜치: `fix/e-accounts-lazy`
- 검증 시점 commit: `7849171`
- DB: MySQL 8.0.46 / HikariCP pool 120
- `placeOrder` isolation: **READ-COMMITTED** (서버 기본은 REPEATABLE-READ)
- 금액은 `BigDecimal`, 수량은 정수형으로만 비교

> **결과 표현 원칙**
> 이 문서는 **"테스트한 범위에서 violation 이 관측되었는가"** 만 기록한다.
> "버그 완전 제거"라고 말하지 않는다. 테스트하지 않은 실행 순서·부하·데이터에서
> 위반이 없음을 증명하지 않는다.


## 포트폴리오 캡처용 핵심 수치 (한 화면 요약)

| # | 시나리오 | 조건 | 관측된 violation |
|---|---|---|---|
| 1 | **중복 체결 race (최소)** | SELL 1주 ← BUY 1주 × 2 동시 | **0** |
| 2 | **Overfill stress** | SELL 10주 / 보유 100주 ← BUY 1주 × **100 동시** | **0** |
| 3 | **PARTIAL chain** | BUY 100 ← SELL 30→20→10→40 | **0** |
| 4 | **race 반복** | 동일 중복 체결 race **1,000회** | **0 / 1000 회차** |
| 5 | **randomized (1차)** | 20 seeds × 500 ops × 8 workers = **10,000 ops** | **0** |
| 6 | **randomized (확장)** | 20 seeds × 1,000 ops × **16 workers** = **20,000 ops** | **0** |

부가 지표: 전 시나리오에서 **deadlock 0, lock timeout 0**.


> **저장소에 포함된 범위**
> 실험 원본(k6 raw JSON·HTML report·실행 로그, 조사 과정 문서)은 **로컬에 보관**하며
> 저장소에는 **집계 결과와 정합성 raw CSV 만 공개**한다.
> 따라서 이 문서가 참조하는 일부 원본 파일은 저장소에 없다.

---

## 검증한 불변식

| # | 불변식 | 검사 위치 |
|---|---|---|
| 1 | 총 체결량 ≤ 최초 주문량 (overfill 금지) | `OVERFILL` |
| 2 | remaining = original − executed | `REMAINING_MISMATCH` |
| 3 | remaining = 0 → `COMPLETED` | `STATUS_NOT_COMPLETED` |
| 4 | 0 < remaining < original → `PARTIAL` | `STATUS_NOT_PARTIAL` |
| 5 | Holdings 총량 보존 (체결은 이전일 뿐 생성·소멸 없음) | `HOLDINGS_NOT_CONSERVED` |
| 6 | 잔액 총합 보존 (매수 출금 = 매도 입금) | `BALANCE_NOT_CONSERVED` |

**계산 방식**: 금액은 전부 `BigDecimal.compareTo`(부동소수 비교 없음), 수량은 `int`.

### 오라클 자체 검증도 함께 수행
- 참가자 외 Holdings 혼입 → `ORACLE_FOREIGN_HOLDINGS` = 0
- 타 종목 Holdings 혼입 → `ORACLE_OTHER_STOCK_HOLDINGS` = 0
- Holdings 행 중복 → `HOLDINGS_ROW_COUNT` 위반 0

---

## 시나리오별 상세

### 1~3. 결정론적 시나리오
```
C1 중복 체결      : Trade 1건, 체결량 1, 보유 10→9, SELL COMPLETED/0
C5 Overfill       : 체결량 ≤ 10, overfill 0, 보유+체결 = 100 (일치)
C6 PARTIAL chain  : 잔량 70→50→40→0, 상태 PARTIAL×3 → COMPLETED, 체결합 100
C3 롤백           : 예수금 부족 시 주문/보유/계좌/체결 전부 무변화
```

> **C5 참고**: 체결률은 3/10 이다. 100 스레드가 동시에 출발해 대부분이
> SELL 주문 생성 이전 스냅샷을 읽어 후보를 보지 못하기 때문이며,
> 순차 실행 시 10/10 전량 체결됨을 별도 진단으로 확인했다.
> **자산 손상이 아니라 처리량 특성**이므로 안전성 판정과 분리해 기록한다.

### 4. race 1,000회 반복
```
위반이 관측된 회차 = 0 / 1000
deadlock = 0 / lockTimeout = 0 / 기타 실패 = 0
```
매 회차 독립 데이터(고유 종목코드)를 사용해 회차 간 간섭을 제거했다.

### 5~6. seed 기반 randomized
```
1차 : 20 seeds(1000~1019) × 500 ops × 8 workers  = 10,000 ops → 위반 0
확장: 20 seeds(1000~1019) × 1,000 ops × 16 workers = 20,000 ops → 위반 0
```
- **고정 seed 목록**을 쓴다. seed 없이 무작위로 돌리지 않는다.
- BUY/SELL, 주문 수량(1~5), 사용자(4명 중)를 seed 로 결정한다.
- 실패 시 seed·operation history·위반 내역을
  파일로 저장해 **동일 seed 로 replay** 가능하다.
  (이번 실행에서는 실패가 없어 파일이 생성되지 않았다)

> **seed 재현성의 한계**: 동일 seed 는 **operation sequence** 만 재생성한다.
> thread 스케줄링·DB 락 획득 순서까지 고정하지는 않는다.

---

## 검증 중 철회한 항목 (정직한 기록)

**사용자별 체결량·금액 대조**를 추가하려 했으나 **철회했다.**

- 시도 결과 158~160건의 `USER_*_MISMATCH` 가 나왔다.
- 원인 조사: `Trade` 는 `newOrder` 한쪽에만 기록되고(`TradeService:114`),
  `Order.quantity` 는 체결 시 파괴적으로 갱신된다. 따라서
  **"이 사용자가 실제로 몇 주를 체결했는가"를 DB 만으로 재구성할 수 없다.**
- 같은 실행에서 `HOLDINGS_NOT_CONSERVED` / `BALANCE_NOT_CONSERVED` 는 **0** 이었다.
  즉 **시스템 전체 자산은 보존**되며, 실패한 것은 내 기대값 산출식이었다.
- **production 결함이 아니라 스키마의 관측 한계**로 판단해 철회하고 사유를 코드에 남겼다.

사용자별 대조를 하려면 `Trade` 에 양쪽 주문을 기록하거나 `original_quantity` 를
보존해야 하며, 이는 production 변경이므로 진행하지 않았다(backlog 후보 D).

---

## 결과 표현

**테스트한 범위에서 정의한 불변식 위반이 관측되지 않았다.**

- 결정론적 시나리오 4종
- 동일 race 1,000회 반복
- randomized 20 seeds × 최대 1,000 ops × 최대 16 workers (누적 30,000 ops)

이는 **버그가 완전히 제거되었음을 뜻하지 않는다.** 테스트하지 않은 실행 순서,
더 높은 동시성, 다른 operation history 에서의 동작은 검증되지 않았다.

---

## 남은 한계

- **C5 체결률 30%** — 안전성 불변식은 충족하나 처리량 특성으로 남아 있음
- **사용자별 대조 불가** — 스키마 한계 (위 참조)
- **backlog 미착수** — 후보 D(Trade 양쪽 기록), F(매도 사전검증 TOCTOU),
  G(평단가 미갱신), H(realizedProfit 계산)
- 원시 기록은 `docs/experiments/raw/final_correctness.csv` 에 append-only 로 보존
