# 거래 정합성 개선 최종 보고서

> 이 작업은 기존 팀 프로젝트 종료 후, 주문 상태 전이 및 동시 체결 정합성 문제를
> 개인적으로 재현·분석·개선한 기록입니다.


> **결론 요약**: B2 를 최종 동시성 제어 방식으로 확정하고 회귀·일반화 검증을 수행했다.
> 결정론적 검증(C4~C6)은 모두 통과했으나, **seed 기반 randomized 검증(C7)에서
> 자산 보존 불변식 위반이 관측**되어 성능(k6) 단계로 넘어가지 않았다.



> **저장소에 포함된 범위**
> 실험 원본(k6 raw JSON·HTML report·실행 로그, 조사 과정 문서)은 **로컬에 보관**하며
> 저장소에는 **집계 결과와 정합성 raw CSV 만 공개**한다.

---

## 1. Before — 기존 버그와 정량적 증거

### 기존 거래 기능
`OrderService.placeOrder()` → `OrderMatchingService.match()` → `TradeService.trade()`
세 메서드가 **하나의 물리 트랜잭션**으로 동작한다(`TransactionBoundaryProbeTest` 실측).

### 발견된 정합성 결함 (재현 테스트로 확정)

| 결함 | 증거 | 재현 |
|---|---|---|
| **동일 Order 중복 체결** | 1주짜리 SELL 주문이 2주 체결. 매도자 보유 10→8, 잔액 +2,000원 | `DuplicateFillConcurrencyTest`, 5/5 재현 |
| **PARTIAL 주문 영구 미매칭** | BUY 7주(PARTIAL)와 SELL 7주(PENDING)가 나란히 남아 체결 안 됨 | `PartialOrderRematchTest`, 단일 스레드 결정론적 |
| **스키마 드리프트** | `accounts.realized_profit`, `users.profile_image` 가 `V1__init.sql` 에 없음 | 실제 SQL 에러로 확인 |

기존 `TradeServiceConcurrencyTest` 는 `assertThat(...).isGreaterThanOrEqualTo(0)` 같은
느슨한 assertion 이라 이 결함들을 잡지 못했고, 애초에 placeholder 오류로 **실행조차 되지 않았다**.

---

## 2. 해결 후보와 실제 비교 과정

6개 후보를 검토해 2개로 좁히고, 각각 1차·2차 두 번 설계했다.

| 후보 | 1차 결과 | 실패 원인 (실측) | 2차 변경 | 2차 결과 |
|---|---|---|---|---|
| **A** Order 비관적 락 | S3 N=100 체결률 1%, deadlock 99 | `FOR UPDATE+ORDER BY+LIMIT` 이 PRIMARY 인덱스로 48행 잠금. 자기 INSERT 행이 순환 대기 참여 (lock graph 채집) | `excludeOrderId` 제거(중복 조건임을 3건 테스트로 증명 후) | **개선 없음.** deadlock 99→97. 원인이 인덱스 선택이 아니라 "INSERT 한 테이블을 같은 트랜잭션에서 FOR UPDATE 스캔"하는 구조 자체 |
| **B** 조건부 UPDATE | S3 N=100 체결률 7% | ① RR 스냅샷에 갇혀 재조회가 소비된 후보 반복 반환 ② RuntimeException 이 rollback-only 유발 | ① placeOrder 에만 READ_COMMITTED ② `TradeResult` 값 반환 ③ 조건부 UPDATE 를 자산 검증보다 앞으로 | **체결률 100%, deadlock 0** |

두 차례 모두 **실패 원인을 lock graph / 프로브 테스트로 실측**한 뒤 재설계했다.
1차 결과는 실험 기록(로컬 보관), 프로브 조사 기록(로컬 보관) 에 그대로 보존돼 있다.

---

## 3. B2 선택 및 선택 근거

**사전에 고정한 우선순위**(결과를 본 뒤 바꾸지 않음): 정합성 → deadlock/안정성 → p95/처리량 → 구현 복잡도

| 순위 | 기준 | 판정 |
|---|---|---|
| 1 | 정합성 | A1·B1·A2·B2 **전부 통과** → 동점 |
| 2 | deadlock | A1 99 · A2 99 vs **B1 0 · B2 0** → **A 계열 탈락** |
| 3 | 체결 처리량 | B1 30.5 vs **B2 84.3 체결/초** (2.8배) → **B2 선택** |
| 4 | 구현 복잡도 | 3번에서 결정되어 미적용 |

**p95 지연은 판정에서 제외했다.** N=100 에서 A2 의 성공 표본은 1건, B2 는 100건으로
표본 수가 100배 달라 비교가 성립하지 않는다.

---

## 4. Deterministic correctness (C4~C6)

`branch=final/b2-conditional-update`, `commit=5959494`, `isolation=READ_COMMITTED(placeOrder only)`, `pool=120`

| 시나리오 | 조건 | 결과 | 위반 |
|---|---|---|---|
| **C4** | SELL 1주 ← BUY 1주 × 2 동시, **1,000회 반복** | **PASS** | **0 / 1000 회차** (deadlock 0, lockTimeout 0) |
| **C5** | SELL 10주 ← BUY 1주 × **100 동시** (seller 보유 100) | **PASS** | **0** (안전성 기준) |
| **C6** | BUY 100 ← SELL 30→20→10→40 | **PASS** | **0** (잔량 70→50→40→0, 체결합 100) |

### C4 표현에 대한 주의
이 결과는 **"1,000회 재현 시도에서 위반이 관측되지 않았다"**는 뜻이다.
안전성이 증명됐다거나 race condition 을 완전히 제거했다는 뜻이 **아니다.**

### C5 관측: 안전성과 체결률의 분리
안전성 불변식은 모두 충족했으나(overfill 0, 보유+체결=100, 잔량=최초-체결),
**체결률은 3/10 (30%)** 였다. 100개 스레드가 배리어에서 동시 출발해
대부분이 SELL 주문 생성 이전 스냅샷을 읽어 후보를 보지 못했기 때문이다.
순차 실행 시에는 **10/10 전량 체결**됨을 별도 진단 테스트로 확인해
로직 결함이 아님을 보였다. 이는 자산 손상이 아니라 **처리량 특성**이다.

### C6 귀속
이 결과는 **B2(동시성 제어)의 효과가 아니라 공통 matching logic 수정**
(`status IN ('PENDING','PARTIAL')` + 매칭 loop)의 효과다. 기존 결론을 유지한다.

---

## 5. Randomized invariant 결과 (C7)

조건: **고정 seed 1000~1019 (20개) × 500 ops × worker 8 = 10,000 연산**

| 항목 | 값 |
|---|---|
| 총 연산 | 10,000 |
| **불변식 위반** | **40** |
| 실패 seed | **20개 전부** (1000~1019) |
| 위반 유형 | `HOLDINGS_NOT_CONSERVED`=20, `BALANCE_NOT_CONSERVED`=20 |
| 소요 시간 | 75,003ms |

### 관측된 위반의 성격

```
seed=1000, workers=2, ops=30
HOLDINGS_NOT_CONSERVED: actual=3994 expected=4000   (6주 소실)
BALANCE_NOT_CONSERVED : actual=400006000 expected=400000000  (6,000원 증가)
```

주식 6주가 사라지고 정확히 그 가치(6주 × 1,000원)만큼 돈이 생겼다.

### 진단으로 확인한 사실

| 확인 | 결과 |
|---|---|
| worker 수 의존성 | **workers=1 → 위반 0. workers≥2 → 위반 발생** |
| 재현성 | 동일 seed 3회 연속 **정확히 동일한 차이(6주/6,000원)** |
| 예외 발생 | **없음.** 30개 연산 전부 `OK` 반환 |
| Holdings 행 중복 | 없음 (`HOLDINGS_ROW_COUNT` 위반 0) |
| deadlock | 해당 실행에서 신규 deadlock 미발생 (`SHOW ENGINE INNODB STATUS` 타임스탬프 미갱신) |

**동시성이 있어야만 발생하고, 동일 seed 에서 결정론적으로 재현되며, 예외 없이 조용히
자산이 사라진다.** C1~C6 이 모두 통과한 상태에서 randomized 테스트가 잡아낸 결함이다.

### 근본 원인: 미확정 (추정하지 않음)
Holdings 테이블의 락 순서 역전으로 인한 deadlock 그래프를 한 차례 채집했으나
(`index FKo0m56qvi5yyl5ikolvm7ih20o of table holdings`, 두 트랜잭션이
user 27861/27859 를 반대 순서로 잠금), **위반이 발생한 실행에서는 신규 deadlock 이
관측되지 않았다.** 따라서 이 lock graph 가 위반의 원인이라고 단정할 수 없다.

가능성이 남아 있는 방향(**전부 미검증**):
- 후보 E(Holdings/Accounts 락 순서) — backlog 로 분류돼 이번 범위에 포함되지 않았다
- 후보 C(Holdings 중복 생성) — C7 은 사전 생성으로 이 경로를 타지 않도록 했다
- `Trade` 가 `newOrder` 한쪽에만 기록되는 구조(후보 D)로 인한 집계 방식 문제

**실패 seed 20개의 전체 operation history 를 실패 seed history 파일(로컬 보관) 에 보존**했으므로
동일 seed 로 replay 하며 원인을 좁힐 수 있다.

---

## 6. API-level k6 결과

**N/A — 수행하지 않았다.**

정확성 검증(C7)이 실패했으므로 성능 단계로 넘어가지 않았다.
"정합성을 확보한 B2 가 부하 증가 시 어떤 비용을 갖는가"라는 질문은
정합성이 확보된 뒤에야 의미가 있다.

관련 지표(achieved req/s, http_req_duration p50/p95/p99, dropped iterations,
trade executed rate, retry per successful trade 등)는 **전부 N/A** 이며 추정하지 않는다.

---

## 7. Before / After 핵심 수치

모든 값은 `raw/*.csv` 에서 역추적 가능하다.

| 항목 | Before (대책 없음) | After (B2) | 출처 |
|---|---|---|---|
| 1주 주문 중복 체결 | **2주 체결** (5/5 재현) | **1주** (C4 1,000회 위반 0) | `correctness.csv`, `final_correctness.csv` |
| PARTIAL 재매칭 | **영구 미체결** | 70→50→40→0 정상 | `final_correctness.csv` C6 |
| S3 N=100 체결률 | 7% | **100%** | `performance_s3.csv` |
| S3 N=100 deadlock | 0 | **0** | `performance_s3.csv` |
| S4 성공 | 25/40 | **40/40** | `performance_s4.csv` |
| S4 deadlock | 0 | **0** | `performance_s4.csv` |
| **자산 보존 (randomized)** | **위반 37~40건** | **위반 40건** | `final_correctness.csv` C7 |

> **정정(추가 측정 완료)**: 이 항목은 최초 보고 시 "Before N/A"로 기록했으나,
> 이후 동일 seed·동일 테스트 코드로 BASE 브랜치(`attrib/base-comparison`)를 실행해
> **개선 전에도 20개 seed 전부에서 위반이 발생함**을 확인했다.
> 상세 조사 기록은 로컬에 보관한다.
>
> 위반 규모는 BASE 가 더 크다: Trade 건수 139~240(B2 는 17~22),
> Holdings 차이 -80~+13(B2 는 +2~+11). BASE 는 주식이 **늘어나는**(복제) 경우까지 있다.
> **B2 는 자산 복제는 막았으나 자산 감소 경로는 막지 못했다.**

---

## 8. 기술 선택으로 보장하게 된 불변식

C4~C6 범위에서 **관측된 위반이 없는** 불변식:

| 불변식 | 검증 |
|---|---|
| Σ executedQuantity ≤ originalOrderQuantity (overfill 금지) | C4 1,000회, C5 100 concurrent |
| remainingQuantity = originalQuantity − Σ executedQuantity | C4, C5, C6 |
| remaining=0 ↔ COMPLETED, 0<remaining<original ↔ PARTIAL | C4, C5, C6 |
| 매도자 보유 감소량 = 실제 체결량 | C4, C5 |
| 자산 검증 실패 시 조건부 UPDATE 변경까지 롤백 | C3 (2차 단계) |
| PARTIAL 주문의 재매칭 | C6 (단, 공통 matching logic 의 효과) |

**보장되지 않은 것**: 다자간 랜덤 동시 거래에서의 **자산 총량 보존**(C7 위반).

---

## 9. 남은 Trade-off와 한계

### 확인된 것
- **C7 자산 보존 위반이 미해결이다.** 원인 미확정이며, 이것이 해결되기 전까지
  B2 를 운영에 적용할 수 없다. 실패 seed 20개의 history 가 보존돼 있어 replay 가능하다.
- **C5 체결률 30%**: 동시 진입 시 스냅샷 타이밍으로 후보를 못 보는 요청이 생긴다.
  안전성 문제는 아니지만 처리량 특성으로 기록한다.
- **B2 의 절대 지연이 크다**: S3 N=100 에서 1.2초. 재조회 루프의 대가다.

### 측정하지 않은 것 (추정 금지)
- k6 API-level 성능: **N/A**
- 대책 없는 상태의 C7 자산 보존: **N/A**
- 실제 production SLO: **알 수 없음.** 모든 성능 수치는 동일 로컬 환경 상대 비교로만 사용했다.

### Backlog (이번 범위 제외)
후보 C(Holdings 중복 생성), E(Holdings/Accounts 락 순서), F(매도 사전검증 TOCTOU),
G(평단가 미갱신), H(realizedProfit 계산). **C7 위반의 원인이 이 중 하나일 가능성이 있다.**

### 환경 한계
- `performance_schema` 권한 없음 → 잠긴 행 목록 조회 불가
- 전역 상태 카운터는 다른 세션 활동에 오염될 수 있음
- 스키마 드리프트 2건은 테스트 DB 에만 ALTER 했고 운영 마이그레이션 미반영

---

## 10. 다음 단계 제안

1. **C7 위반 원인 규명** — 실패 seed history 파일(로컬 보관) 부터 replay 하며 최소 재현으로 축소
2. 원인이 backlog 후보 중 하나라면 그 후보를 이번 범위로 승격해 수정
3. 수정 후 C4~C7 전체 재실행
4. **C7 통과 후에야** k6 API-level 성능 검증 진행
