# Evidence Manifest

> 이 작업은 기존 팀 프로젝트 종료 후, 주문 상태 전이 및 동시 체결 정합성 문제를
> 개인적으로 재현·분석·개선한 기록입니다.


포트폴리오 캡처 페이지의 모든 수치를 repository 의 실제 파일과 연결한다.
**추정값 없음.** 확인되지 않은 값은 N/A 로 표기한다.

---

## 01-correctness.html

| 포트폴리오 주장 | source document | raw evidence | commit |
|---|---|---|---|
| 중복 체결 race — violation 0 | `docs/experiments/CORRECTNESS-FINAL.md` | `raw/correctness.csv` (scenario=C1, variant=FIX-E-FINAL: overfill 0, 체결합 1) | 7849171 |
| Overfill (SELL 10/보유 100/BUY×100) — violation 0 | 동상 | `raw/final_correctness.csv` (C5, conc=100, invariant_violations=0) | 7849171 |
| PARTIAL chain — violation 0 | 동상 | `raw/final_correctness.csv` (C6, invariant_violations=0) | 7849171 |
| race 1,000회 — 0 / 1000 | 동상 | `raw/final_correctness.csv` (C4, repetition=1000, total_requests=2000, violations=0) | 7849171 |
| randomized 1차 10,000 ops — violation 0 | 동상 | `raw/final_correctness.csv` (C7, conc=8, total_requests=10000, violations=0) | 7849171 |
| randomized 확장 20,000 ops — violation 0 | 동상 | `raw/final_correctness.csv` (C7, conc=16, total_requests=20000, violations=0) | 7849171 |
| deadlock 0 / lock timeout 0 | 동상 | `raw/final_correctness.csv` (C4/C5/C6 의 deadlocks=0, lock_timeouts=0) | 7849171 |
| MySQL 8.0.46 | `docs/experiments/k6/environment.md` | 실행 시 `SELECT VERSION()` | — |
| placeOrder = READ COMMITTED | `docs/experiments/k6/BEFORE-AFTER-100RPS.md` (isolation 절) | `raw/isolation_probe.csv` | 7849171 |

> **주의**: C7(randomized)의 `deadlocks`/`lock_timeouts` 는 CSV 상 **N/A** 다.
> 해당 시나리오는 개별 요청의 errno 를 집계하지 않는다.
> 페이지의 "deadlock 0" 은 **결정론적 시나리오(C4/C5/C6) 기준**이며 그렇게 각주를 달았다.

> **철회 기록**: `raw/final_correctness.csv` 의 C7 행 중 violations=158, 160 은
> 사용자별 대조 검사를 추가했다가 철회한 시도다. 검사식 오류였고
> 같은 실행에서 총량 보존은 0 이었다. 상세는 `CORRECTNESS-FINAL.md` "철회한 항목".

---

## 02-before-after.html

| 포트폴리오 주장 | source document | raw evidence | commit |
|---|---|---|---|
| **Before**: 1주 SELL 이 2주 체결, Trade 2건, 보유 10→8 | `docs/experiments/FINAL-REPORT.md:19`<br>1차 실험 기록(로컬 보관) (S1 FAILED, Trade 2건) | **N/A — CSV 없음**<br>재현 테스트 `DuplicateFillConcurrencyTest` 실행 결과가 Markdown 에만 기록됨 (CSV 수집 체계는 이후 구축) | 69137f0 |
| Before 5/5 재현 | `docs/experiments/FINAL-REPORT.md:19` | 동상 (N/A) | 69137f0 |
| **After**: Trade 1건, 체결량 1주, 보유 10→9 | `docs/experiments/CORRECTNESS-FINAL.md` | `raw/correctness.csv` scenario=C1, variant=FIX-E-FINAL<br>(original=1, executed_sum=1, overfill=0, holdings 10→9, status=COMPLETED) | 7849171 |
| After: race 1,000회 위반 0 | 동상 | `raw/final_correctness.csv` (C4) | 7849171 |
| PARTIAL Before: BUY 7(PARTIAL) + SELL 7(PENDING) 미체결 | `docs/experiments/FINAL-REPORT.md:20` | **N/A — CSV 없음** (`PartialOrderRematchTest` 결과가 Markdown 에만) | 69137f0 |
| PARTIAL After: 70→50→40→0 COMPLETED | `docs/experiments/CORRECTNESS-FINAL.md` | `raw/final_correctness.csv` (C6, partial_chain 컬럼) | 7849171 |

---

## 03-performance.html

모든 값은 `docs/experiments/k6/BEFORE-AFTER-100RPS.md` 의 **5회 중앙값** 행에서 인용.

| 지표 | BASE | FIX-E | raw evidence |
|---|---|---|---|
| achieved rate (req/s) | 87.03 | 99.57 | k6 summary JSON 10건 (로컬 보관) |
| dropped iterations | 729 | 0 | 동상 |
| p50 (ms) | 40.76 | 21.80 | 동상 |
| p95 (ms) | 9,435.32 | 381.03 | 동상 |
| p99 (ms) | 13,461.42 | 712.30 | 동상 |
| http_req_failed | 0.0 | 0.0 | 동상 |

| 조건 주장 | source | 확인 방법 |
|---|---|---|
| iteration 1회 = `POST /api/orders` 1회 | `k6/trading-load.js` | default 함수 내 http 호출 1건 (grep 확인) |
| duration 120s, 5 runs/variant | `k6/BEFORE-AFTER-100RPS.md` 헤더 | — |
| HikariCP 100 | `src/main/resources/application-perf.yml` | — |
| isolation 차이 (RR vs RC) | `k6/BEFORE-AFTER-100RPS.md` isolation 절 | `raw/isolation_probe.csv` |

---

## 04-decision.html

| 포트폴리오 주장 | source document | raw evidence | commit |
|---|---|---|---|
| 비관적 락 deadlock S3 N=100 = 99건 | 실험 요약 기록(로컬 보관) | `raw/performance_s3.csv` | 2adab9d |
| Conditional UPDATE deadlock = 0건 | 동상 (B2 행) | `raw/performance_s3.csv` | 2adab9d |
| S4 deadlock 39 vs 0 | 2차 실험 기록(로컬 보관) (S4 표) | `raw/performance_s4.csv` | 2adab9d |
| S3 N=100 체결률 1% vs 100% | 2차 실험 기록(로컬 보관) (S3 표) | `raw/performance_s3.csv` | 2adab9d |
| S4 성공 1/40 vs 40/40 | 2차 실험 기록(로컬 보관) (S4 표) | `raw/performance_s4.csv` | 2adab9d |
| 구현 변경량 3파일 +64/−3, 4파일 +105/−12 | 2차 실험 기록(로컬 보관) (구현 변경량 절) | `git diff --stat` | 2adab9d |
| excludeOrderId 제거 후 deadlock 99→97 | 2차 실험 기록(로컬 보관) (A2-3 절) | `raw/performance_s3.csv` | 2adab9d |
| 원인 = INSERT 한 테이블을 FOR UPDATE 스캔 | 2차 실험 기록(로컬 보관) (lock graph 절) | `SHOW ENGINE INNODB STATUS` 채집본 (문서 내 인용) | 2adab9d |
| p95 판정 제외 (표본 1 vs 100) | 실험 요약 기록(로컬 보관) | `raw/performance_s3.csv` | 2adab9d |
| 판정 순서 사전 고정 | 지표 정의서(로컬 보관) | — | — |

---

## 확인할 수 없어 N/A 로 둔 값

| 항목 | 이유 |
|---|---|
| Before(69137f0) 중복 체결 raw CSV | 그 시점에는 CSV 수집 체계가 없었다. Markdown 기록만 존재 |
| Before PARTIAL 미체결 raw CSV | 동상 |
| C7 randomized 의 deadlock/lock timeout | 해당 테스트가 개별 요청 errno 를 집계하지 않음 (CSV 상 N/A) |
| `candidate_retry` (k6) | 응답이 재시도 횟수를 노출하지 않음 |
| 사용자별 체결량·금액 대조 | Trade 가 한쪽 주문만 기록하고 quantity 가 파괴적 갱신되어 DB 로 재구성 불가 |

---

## 저장소에 포함된 범위

이 브랜치는 포트폴리오 공개용으로 다음만 포함한다.

| 포함 | 제외 (로컬 보관) |
|---|---|
| production source + DB migration | k6 raw JSON (813MB) |
| 정합성 회귀·randomized 테스트 | k6 HTML report / 실행 로그 |
| 정합성 raw CSV | 조사 과정 문서 (1차·2차 실험, 프로브 기록) |
| 집계 결과 문서 · 캡처 페이지 | 벤치마크 전용 endpoint/config |

따라서 위 표의 일부 source 는 저장소에 없고 로컬에만 있다.
저장소에 있는 근거는 `docs/experiments/raw/*.csv` 와 집계 Markdown 이다.
