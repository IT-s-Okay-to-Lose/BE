-- 매칭 쿼리 전용 복합 인덱스.
--
-- 근거: docs/experiments/EXPLAIN-baseline.md
--   인덱스 없이는 1건을 얻기 위해 10,000행을 스캔한다(EXPLAIN ANALYZE 실측).
--   FOR UPDATE 는 스캔 중 접근한 레코드를 잠그므로, 인덱스가 없으면
--   LIMIT 1 을 붙여도 "1건만 잠근다"는 의도가 성립하지 않는다.
--
-- 컬럼 순서: 등치 조건(stock_code, order_type, price, status)을 앞에 두고
--            정렬 키(created_at, order_id)를 뒤에 둔다.
--            이 순서 덕분에 가격 버킷별로 락 범위가 분리된다(행동 측정으로 확인).
CREATE INDEX idx_orders_matching
    ON orders (stock_code, order_type, price, status, created_at, order_id);
