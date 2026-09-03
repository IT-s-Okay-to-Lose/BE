package com.example.iotl.trading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 실험 실행 1회의 재현 컨텍스트.
 *
 * <p>모든 측정 레코드에 run_id / timestamp / branch / commit / scenario /
 * concurrency / pool size / isolation / seed / repetition 을 붙여 저장한다.
 * 이 정보 없이는 수치를 재현할 수 없으므로 CSV 의 모든 행에 포함된다.
 *
 * <p>branch/commit 은 시스템 프로퍼티(-Dbench.branch, -Dbench.commit)로 주입한다.
 * 테스트가 git 을 직접 호출하지 않도록 해서 실행 환경 의존을 줄인다.
 * 주입되지 않으면 "N/A" 로 남긴다(추정하지 않는다).
 */
public final class RunContext {

    /** 이번 JVM 실행 전체를 식별한다. 같은 gradle test 실행 내 모든 레코드가 공유한다. */
    public static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);

    public static final String VARIANT = prop("bench.variant", "N/A");
    public static final String BRANCH = prop("bench.branch", "N/A");
    public static final String COMMIT = prop("bench.commit", "N/A");
    public static final String POOL_SIZE = prop("bench.pool", "N/A");
    public static final String ISOLATION = prop("bench.isolation", "N/A");

    /**
     * 시드. 현재 테스트는 모두 deterministic 이라 난수를 쓰지 않으므로 "N/A" 가 기본이다.
     * randomized test 를 도입하면 -Dbench.seed 로 주입하고 실패 이력과 함께 보존한다.
     */
    public static final String SEED = prop("bench.seed", "N/A");

    private static final Path OUT_DIR = Paths.get("docs/experiments/raw");

    private RunContext() {
    }

    private static String prop(String key, String def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    /** 공통 컨텍스트 컬럼. 모든 CSV 가 이 컬럼들로 시작한다. */
    public static Map<String, String> base(String scenario, int concurrency, int repetition) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("run_id", RUN_ID);
        m.put("timestamp", Instant.now().toString());
        m.put("variant", VARIANT);
        m.put("branch", BRANCH);
        m.put("commit", COMMIT);
        m.put("scenario", scenario);
        m.put("concurrency", String.valueOf(concurrency));
        m.put("pool_size", POOL_SIZE);
        m.put("isolation", ISOLATION);
        m.put("seed", SEED);
        m.put("repetition", String.valueOf(repetition));
        return m;
    }

    /**
     * CSV 한 행을 append 한다. 파일이 없으면 헤더를 먼저 쓴다.
     * 기존 파일을 덮어쓰지 않는다(A1/A2/B1/B2 결과 보존 요구사항).
     */
    public static synchronized void append(String fileName, Map<String, String> row) {
        try {
            Files.createDirectories(OUT_DIR);
            Path f = OUT_DIR.resolve(fileName);
            boolean isNew = !Files.exists(f);
            StringBuilder sb = new StringBuilder();
            if (isNew) {
                sb.append(String.join(",", row.keySet())).append('\n');
            }
            sb.append(row.values().stream()
                .map(RunContext::escape)
                .reduce((a, b) -> a + "," + b).orElse(""))
                .append('\n');
            Files.writeString(f, sb.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String escape(String v) {
        if (v == null) {
            return "N/A";
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }
}
