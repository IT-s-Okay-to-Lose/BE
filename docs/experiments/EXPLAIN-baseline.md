# EXPLAIN 분석: 매칭 쿼리 실행계획 (공통 인덱스 추가 근거)

**측정 환경 (실측)**
- MySQL 8.0.46, 격리수준 REPEATABLE-READ
- `max_connections = 151`, `innodb_lock_wait_timeout = 50`
- orders 20,000행 / 2종목 / 50가격 / 4상태 혼합
- `performance_schema` 는 test_user 권한 부족으로 조회 불가 → 락 범위는 **행동 기반 측정**으로 대체

---

## 1. BEFORE — 인덱스 없음

기존 인덱스는 PK와 FK 2개(`stock_code`, `user_id`)뿐이며,
`(status, order_type, price)` 를 커버하는 인덱스가 없다.

```
-> Limit: 1 row(s)  (actual time=14.1..14.1 rows=1)
  -> Sort: created_at, order_id, limit input to 1 row/chunk  (actual time=14.1..14.1)
    -> Filter: order_type='SELL' and status in (...) and price=1000  (actual rows=400)
      -> Index lookup using FK(stock_code='EXPL01')  (actual time=0.308..11.1 rows=10000)
```

**핵심: 1행을 얻으려고 10,000행을 읽는다.**

`FOR UPDATE` 는 스캔 과정에서 접근한 레코드에 락을 건다.
따라서 인덱스 없이 `LIMIT 1` 만 붙이면 **"1건만 잠근다"는 의도가 성립하지 않는다.**
이것이 사용자가 지적한 "인덱스가 부적절하면 불필요하게 많은 레코드를 스캔/잠글 수 있다"는
우려가 실제로 성립함을 보여주는 근거다.

---

## 2. AFTER — 공통 인덱스 추가

```sql
CREATE INDEX idx_orders_matching
    ON orders (stock_code, order_type, price, status, created_at, order_id);
```

```
-> Limit: 1 row(s)  (actual time=0.817..0.817 rows=1)
  -> Sort: created_at, order_id, limit input to 1 row/chunk  (actual time=0.816..0.816)
    -> Index range scan using idx_orders_matching  (actual time=0.191..0.757 rows=400)
```

| 지표 | BEFORE | AFTER | 개선 |
|---|---|---|---|
| 실제 스캔 행 수 | 10,000 | 400 | **25배** |
| 실행 시간 | 14.1 ms | 0.8 ms | **17배** |
| 접근 방식 | FK 인덱스 + 필터 | 인덱스 레인지 스캔 | — |

### filesort 가 남는 이유
`status IN ('PENDING','PARTIAL')` 은 두 개의 레인지로 분할되어 인덱스 정렬 순서를
그대로 쓸 수 없다. 컬럼 순서를 바꾼 v2(`status` 를 `price` 앞으로)도 동일하게
filesort 가 남았고 성능 차이는 없었다(0.755ms vs 0.817ms).
정렬 대상이 400행 수준이면 비용이 무시할 만하므로 **v1 순서를 채택**한다.

---

## 3. 락 범위 행동 측정 (performance_schema 대체)

`data_locks` 조회 권한이 없으므로, 두 세션을 띄워 **대기 발생 여부**로 락 범위를 역추정했다.

| 실험 | 세션1이 잠근 것 | 세션2가 요청한 것 | 결과 |
|---|---|---|---|
| 동일 가격, 2순위 후보 (`OFFSET 1`) | price=1000 1순위 | price=1000 2순위 | **3.1초 대기 후 timeout** |
| 동일 가격, 2순위 후보 — READ COMMITTED | 동일 | 동일 | **3.1초 대기 후 timeout** (갭락 아님) |
| **다른 가격** | price=1000 1순위 | price=1002 1순위 | **133ms, 대기 없음** |

### 해석
- `ORDER BY ... LIMIT 1 OFFSET 1` 이 막히는 것은 갭락 때문이 아니다.
  READ-COMMITTED 에서도 동일하게 막혔다. 2순위에 도달하려면 1순위 행을
  거쳐가야 하므로 잠긴 행에서 대기하는 것이다.
- **다른 가격 버킷은 서로 간섭하지 않는다.** 인덱스가
  `(stock_code, order_type, price, ...)` 순서라 가격별로 레인지가 분리되기 때문이다.
- 따라서 후보 A(FOR UPDATE LIMIT 1)의 락 경합은 **"동일 종목 + 동일 가격" 범위로 한정**된다.
  이는 S3 벤치마크(동일 종목·동일 가격 경합) 설계와 정확히 일치하며,
  의도적으로 최악 조건을 측정하게 된다.

---

## 4. 결론: 공통 베이스에 인덱스 추가

`idx_orders_matching` 을 **두 후보 공통 베이스**에 포함한다.

근거:
1. 인덱스 없이는 후보 A의 `LIMIT 1` 락 한정 의도가 성립하지 않는다(10,000행 스캔).
2. 후보 B의 조건부 UPDATE 도 후보 조회를 선행하므로 동일하게 이득을 본다.
3. 한쪽에만 적용하면 **성능 비교가 인덱스 유무 비교로 오염**된다.
