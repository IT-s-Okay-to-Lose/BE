package com.example.iotl.trading;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * S3/S4 벤치마크 공용 계측 유틸.
 *
 * <p>두 실험 브랜치가 이 파일을 그대로 공유한다(공통 베이스 커밋).
 * 브랜치 간 test 코드 diff 가 비어 있어야 실험이 공정하다.
 *
 * <p>계측 경로에 대한 실측 근거:
 * <ul>
 *   <li>performance_schema.data_locks / data_lock_waits 는 test_user 권한 부족으로 조회 불가.
 *       따라서 lock wait 는 (1) 전역 카운터 delta 와 (2) JDBC errno 로 이중 측정한다.</li>
 *   <li>Innodb_deadlocks 는 SHOW GLOBAL STATUS 에 노출되지 않는다.
 *       deadlock 은 JDBC SQLState 40001 / errno 1213 으로만 판정한다.</li>
 * </ul>
 */
public final class BenchmarkSupport {

    private BenchmarkSupport() {
    }

    /** delta 로 추적할 MySQL 전역 상태 변수. */
    /** delta 로 의미가 있는 누적 카운터. */
    public static final List<String> COUNTERS = List.of(
        "Innodb_row_lock_waits",
        "Innodb_row_lock_time",
        "Com_select",
        "Com_update",
        "Com_insert",
        "Com_commit",
        "Com_rollback"
    );

    /**
     * delta 가 의미 없는 gauge. 서버 기동 이후 누적 평균/최댓값이므로
     * before/after 절대값을 그대로 기록한다(실측으로 확인: 연속 조회 시 동일값 유지).
     */
    public static final List<String> GAUGES = List.of(
        "Innodb_row_lock_time_avg",
        "Innodb_row_lock_time_max"
    );

    private static final List<String> TRACKED = concat(COUNTERS, GAUGES);

    private static List<String> concat(List<String> a, List<String> b) {
        java.util.List<String> out = new java.util.ArrayList<>(a);
        out.addAll(b);
        return List.copyOf(out);
    }

    /** MySQL errno: 데드락 감지 후 트랜잭션 강제 롤백. */
    public static final int ERR_DEADLOCK = 1213;
    /** MySQL errno: 락 대기 시간 초과. */
    public static final int ERR_LOCK_WAIT_TIMEOUT = 1205;

    public static Map<String, Long> snapshot(DataSource ds) {
        Map<String, Long> out = new LinkedHashMap<>();
        try (var conn = ds.getConnection(); var st = conn.createStatement()) {
            for (String name : TRACKED) {
                try (var rs = st.executeQuery("SHOW GLOBAL STATUS LIKE '" + name + "'")) {
                    if (rs.next()) {
                        out.put(name, Long.parseLong(rs.getString(2)));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("전역 상태 스냅샷 실패", e);
        }
        return out;
    }

    /** 누적 카운터만 delta 를 낸다. gauge 는 delta 를 내지 않는다. */
    public static Map<String, Long> delta(Map<String, Long> before, Map<String, Long> after) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (String k : COUNTERS) {
            out.put(k, after.getOrDefault(k, 0L) - before.getOrDefault(k, 0L));
        }
        return out;
    }

    /** 예외 체인을 훑어 MySQL errno 를 찾는다. 없으면 -1. */
    public static int mysqlErrorCode(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof SQLException sql) {
                return sql.getErrorCode();
            }
            if (c.getCause() == c) {
                break;
            }
        }
        return -1;
    }

    public static boolean isDeadlock(Throwable t) {
        return mysqlErrorCode(t) == ERR_DEADLOCK;
    }

    public static boolean isLockWaitTimeout(Throwable t) {
        return mysqlErrorCode(t) == ERR_LOCK_WAIT_TIMEOUT;
    }

    /** latency 배열에서 백분위수를 구한다(nearest-rank). */
    public static long percentile(long[] sortedAsc, double p) {
        if (sortedAsc.length == 0) {
            return 0L;
        }
        int idx = (int) Math.ceil(p * sortedAsc.length) - 1;
        return sortedAsc[Math.max(0, Math.min(idx, sortedAsc.length - 1))];
    }

    public static long mean(long[] values) {
        if (values.length == 0) {
            return -1L;   // 측정값 없음. 0 으로 쓰면 "0ms" 로 오독된다.
        }
        return Arrays.stream(values).sum() / values.length;
    }

    public static long max(long[] sortedAsc) {
        return sortedAsc.length == 0 ? -1L : sortedAsc[sortedAsc.length - 1];
    }

    /** 표본이 없으면 -1(=N/A). 있으면 nearest-rank 백분위수. */
    public static long pct(long[] sortedAsc, double p) {
        if (sortedAsc.length == 0) {
            return -1L;
        }
        int idx = (int) Math.ceil(p * sortedAsc.length) - 1;
        return sortedAsc[Math.max(0, Math.min(idx, sortedAsc.length - 1))];
    }

    /** -1 을 N/A 문자열로 변환한다. 측정값 없음을 0 과 구분하기 위함. */
    public static String na(long v) {
        return v < 0 ? "N/A" : String.valueOf(v);
    }
}
