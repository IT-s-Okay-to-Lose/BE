# 캡처 체크리스트

각 HTML 을 브라우저에서 열고 **한 장씩** 캡처한다.
저장 위치 권장: `docs/portfolio/capture/shots/`

> 캡처 한 장 = 메시지 하나. 여러 화면을 한 장에 합치지 않는다.

---

## CAPTURE 1 — Correctness
**파일**: `01-correctness.html`

반드시 보여야 할 것:
- 6개 시나리오가 모두 한 화면에
- 각 행의 **violation 0**
- race **1,000회** (0 / 1000)
- randomized **10,000 ops / 20,000 ops**
- 하단 **deadlock 0 / lock timeout 0**
- 하단 **MySQL 8.0.46 · READ COMMITTED · commit 14e67f0**
- 회색 박스의 한계 문장 ("버그가 완전히 제거되었음을 의미하지 않는다")

주의: 한계 문장이 잘리면 안 된다. 브라우저 확대율을 낮춰 전체가 들어오게 한다.

---

## CAPTURE 2 — Before / After
**파일**: `02-before-after.html`

반드시 보여야 할 것:
- 좌우 패널 **Before / After** 가 나란히
- Before: Trade **2건**, executed **2주**, holdings **10 → 8**
- After: Trade **1건**, executed **1주**, holdings **10 → 9**
- 가운데 한 줄 설명 (원자적으로 보호되지 않아 두 번 체결)
- 하단 PARTIAL 표 2줄 (Before 미체결 / After COMPLETED)

---

## CAPTURE 3 — Performance
**파일**: `03-performance.html`

반드시 보여야 할 것:
- 상단 실험 조건 (target 100 req/s · 120s · 5 runs · HikariCP 100)
- 표의 5개 지표 **중앙값** (achieved / dropped / p50 / p95 / p99)
- **SVG 막대 3종** (achieved rate, dropped iterations, p95)
- "Conditional UPDATE 단독 성능이 아니다" 문단
- 하단 **http_req_failed 0** 과 "HTTP 성공과 거래 정합성은 별도의 검증 대상"
- 회색 박스 (BASE 는 correctness defect 버전 / isolation 차이)

주의: 회색 박스를 빼고 캡처하면 **성능 우위로 오독**될 수 있다. 반드시 포함한다.

---

## CAPTURE 4 — Technology decision
**파일**: `04-decision.html`

반드시 보여야 할 것:
- 두 방식(Order PESSIMISTIC_WRITE / Conditional UPDATE) 열 비교
- 평가축 6종이 모두 보일 것
- deadlock 행의 **99건·39건 vs 0건·0건**
- 실험 결과 행의 **1/100 vs 100/100**, **1/40 vs 40/40**
- "선택: Conditional UPDATE" 문단과 **판정 순서를 사전에 고정했다**는 문장
- 하단 각주 (excludeOrderId 제거 후에도 99→97) 와 회색 박스 (p95 판정 제외 사유)

---

## 캡처 순서 (추천)

1. **CAPTURE 2** (Before/After) — 문제가 무엇이었는지 먼저 보여준다
2. **CAPTURE 4** (기술 선택) — 어떻게 골랐는지
3. **CAPTURE 1** (Correctness) — 고친 뒤 무엇을 검증했는지
4. **CAPTURE 3** (Performance) — 그 대가로 성능은 어떻게 되었는지

포트폴리오 서사 순서와 일치한다:
**문제 → 후보 비교 → 정합성 검증 → 성능 영향**

---

## 열기

WSL 에서:
```bash
explorer.exe "$(wslpath -w docs/portfolio/capture/01-correctness.html)"
```

또는 폴더째 열기:
```bash
explorer.exe "$(wslpath -w docs/portfolio/capture)"
```

각 HTML 은 외부 CDN/JS 의존성이 없어 더블클릭으로 바로 열린다.
