import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ─────────────────────────────────────────────────────────────
// 환경변수
//   BASE_URL   대상 서버 (기본 http://localhost:8081)
//   RATE       초당 요청 수 (constant-arrival-rate)
//   DURATION   steady-state 지속 시간
//   PRE_VUS / MAX_VUS   VU 풀
//   RUN_ID / RUN_LABEL  증적 식별자
//   SMOKE=1    smoke test 모드 (아주 작은 부하)
// ─────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const RATE = parseInt(__ENV.RATE || '10', 10);
const DURATION = __ENV.DURATION || '60s';
const PRE_VUS = parseInt(__ENV.PRE_VUS || String(Math.max(10, RATE * 2)), 10);
const MAX_VUS = parseInt(__ENV.MAX_VUS || String(Math.max(50, RATE * 6)), 10);
const RUN_ID = __ENV.RUN_ID || 'local';
const RUN_LABEL = __ENV.RUN_LABEL || 'unlabeled';
const STOCK_CODE = __ENV.STOCK_CODE || 'PERF01';
const PRICE = parseInt(__ENV.PRICE || '1000', 10);
const SMOKE = __ENV.SMOKE === '1';

// ── custom metric: HTTP 200 과 "실제 거래 성공"을 분리한다 ──
const businessSuccess = new Counter('business_success');
const businessFailure = new Counter('business_failure');
const businessSuccessRate = new Rate('business_success_rate');
const tradeExecuted = new Counter('trade_executed');      // COMPLETED/PARTIAL
const orderPending = new Counter('order_pending');         // PENDING (미체결 접수)
const candidateRetry = new Counter('candidate_retry');     // 서버가 재시도를 노출하면 사용
const orderLatency = new Trend('order_latency', true);

export const options = SMOKE
    ? {
        // smoke: 실행 가능성만 확인한다
        scenarios: {
            smoke: {
                executor: 'constant-arrival-rate',
                rate: 5,
                timeUnit: '1s',
                duration: '10s',
                preAllocatedVUs: 5,
                maxVUs: 20,
            },
        },
        summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
    }
    : {
        // 본측정: open model. 응답 지연과 무관하게 유입률을 유지한다.
        scenarios: {
            trading: {
                executor: 'constant-arrival-rate',
                rate: RATE,
                timeUnit: '1s',
                duration: DURATION,
                preAllocatedVUs: PRE_VUS,
                maxVUs: MAX_VUS,
                gracefulStop: '15s',
            },
        },
        // 실제 production SLO 를 모르므로 임의의 latency threshold 를 두지 않는다.
        // 대신 실행 자체의 건전성만 확인한다.
        thresholds: {
            checks: ['rate>0'],
        },
        // p99 를 요약에 포함시킨다(기본 요약에는 p99 가 없다).
        summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
    };

/**
 * fixture 준비 + 토큰 발급.
 * init 컨텍스트에서는 HTTP 호출이 불가하므로 setup() 에서 수행하고,
 * 반환값이 각 VU 로 전달된다.
 */
export function setup() {
    const res = http.post(`${BASE_URL}/perf/setup?users=8&stockCode=${STOCK_CODE}`);
    if (res.status !== 200) {
        throw new Error(`perf setup 실패: HTTP ${res.status} ${res.body}`);
    }
    return {
        startedAt: new Date().toISOString(),
        tokens: JSON.parse(res.body).users,
    };
}

export default function (data) {
    // VU 마다 다른 사용자를 쓴다. 매수/매도를 번갈아 내 체결이 일어나게 한다.
    const tokens = data.tokens;
    const idx = (__VU - 1) % tokens.length;
    const user = tokens[idx];
    const isBuy = (__ITER % 2) === 0;

    const payload = JSON.stringify({
        stockCode: STOCK_CODE,
        orderType: isBuy ? 'BUY' : 'SELL',
        price: PRICE,
        quantity: 1,
    });

    const res = http.post(`${BASE_URL}/api/orders`, payload, {
        headers: { 'Content-Type': 'application/json' },
        cookies: { access: user.token },
        tags: { name: 'POST /api/orders', run_label: RUN_LABEL, run_id: RUN_ID },
    });

    orderLatency.add(res.timings.duration);

    const httpOk = check(res, {
        'HTTP 200': (r) => r.status === 200,
    });

    // ── HTTP 성공과 거래 성공을 분리한다 ──
    let status = null;
    if (httpOk) {
        try {
            status = JSON.parse(res.body).status;
        } catch (e) {
            status = null;
        }
    }

    check(res, {
        '주문 응답에 status 존재': () => status !== null,
    });

    if (status === null) {
        businessFailure.add(1);
        businessSuccessRate.add(false);
    } else {
        businessSuccess.add(1);
        businessSuccessRate.add(true);
        if (status === 'COMPLETED' || status === 'PARTIAL') {
            tradeExecuted.add(1);
        } else if (status === 'PENDING') {
            orderPending.add(1);
        }
    }
}

export function handleSummary(data) {
    const out = {};
    const summaryPath = __ENV.SUMMARY_PATH || `docs/experiments/k6/summaries/${RUN_LABEL}-${RUN_ID}.json`;

    const enriched = {
        run_id: RUN_ID,
        run_label: RUN_LABEL,
        timestamp: new Date().toISOString(),
        base_url: BASE_URL,
        requested_rate_per_sec: RATE,
        duration: DURATION,
        pre_allocated_vus: PRE_VUS,
        max_vus: MAX_VUS,
        stock_code: STOCK_CODE,
        branch: __ENV.GIT_BRANCH || 'N/A',
        commit: __ENV.GIT_COMMIT || 'N/A',
        db: __ENV.DB_INFO || 'N/A',
        isolation: __ENV.DB_ISOLATION || 'N/A',
        metrics: data.metrics,
        root_group: data.root_group,
    };

    out[summaryPath] = JSON.stringify(enriched, null, 2);
    out.stdout = textSummary(data);
    return out;
}

/** 콘솔 요약. 외부 의존성 없이 필요한 값만 출력한다. */
function textSummary(data) {
    const m = data.metrics;
    const g = (name, field) => {
        if (!m[name] || m[name].values === undefined) return 'N/A';
        const v = m[name].values[field];
        return v === undefined ? 'N/A' : (typeof v === 'number' ? v.toFixed(2) : v);
    };
    const lines = [
        '',
        '================ k6 결과 요약 ================',
        `run_label            = ${RUN_LABEL}`,
        `requested rate       = ${RATE} req/s`,
        `duration             = ${DURATION}`,
        '--- HTTP ---',
        `http_reqs            = ${g('http_reqs', 'count')} (${g('http_reqs', 'rate')} /s achieved)`,
        `http_req_failed      = ${g('http_req_failed', 'rate')}`,
        `http_req_duration p50= ${g('http_req_duration', 'med')} ms`,
        `http_req_duration p95= ${g('http_req_duration', 'p(95)')} ms`,
        `http_req_duration p99= ${g('http_req_duration', 'p(99)')} ms`,
        `http_req_duration max= ${g('http_req_duration', 'max')} ms`,
        '--- 실행 ---',
        `iterations           = ${g('iterations', 'count')}`,
        `dropped_iterations   = ${g('dropped_iterations', 'count')}`,
        `vus_max              = ${g('vus_max', 'max')}`,
        '--- 거래(business) ---',
        `business_success     = ${g('business_success', 'count')}`,
        `business_failure     = ${g('business_failure', 'count')}`,
        `trade_executed       = ${g('trade_executed', 'count')}`,
        `order_pending        = ${g('order_pending', 'count')}`,
        `candidate_retry      = ${g('candidate_retry', 'count')}`,
        '=============================================',
        '주의: 이 수치는 동일 로컬 환경의 상대 비교용이며',
        '      production SLO 달성 여부를 뜻하지 않는다.',
        '',
    ];
    return lines.join('\n');
}
