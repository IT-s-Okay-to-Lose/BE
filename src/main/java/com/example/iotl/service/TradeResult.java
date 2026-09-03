package com.example.iotl.service;

/**
 * [B2-1] 체결 시도의 결과.
 *
 * <p>1차 설계는 CAS 0건을 RuntimeException 으로 던졌는데,
 * REQUIRED 내부 메서드에서 언체크 예외가 나가면 물리 트랜잭션이
 * rollback-only 로 마킹되어 바깥에서 catch 해도 커밋이 거부됐다
 * (RollbackOnlyProbeTest 로 실측). 따라서 결과를 값으로 반환한다.
 *
 * <p>의미를 명확히 분리한다.
 * <ul>
 *   <li>{@link #EXECUTED} — 체결 성공</li>
 *   <li>{@link #CANDIDATE_CONSUMED} — <b>정상 경합 결과</b>. 다른 트랜잭션이
 *       내가 본 후보를 먼저 소비했다. 오류가 아니므로 다음 후보로 진행한다.</li>
 *   <li>{@link #INSUFFICIENT_BALANCE}, {@link #INSUFFICIENT_HOLDINGS} —
 *       <b>비즈니스 오류</b>. 자산이 부족해 체결할 수 없다.
 *       경합과 달리 재시도해도 해결되지 않으므로 매칭을 중단한다.</li>
 * </ul>
 */
public enum TradeResult {

    /** 체결 성공. */
    EXECUTED,

    /** 정상 경합: 다른 트랜잭션이 후보를 먼저 소비함. 다음 후보로 진행 가능. */
    CANDIDATE_CONSUMED,

    /** 비즈니스 오류: 매수자 예수금 부족. */
    INSUFFICIENT_BALANCE,

    /** 비즈니스 오류: 매도자 보유 수량 부족. */
    INSUFFICIENT_HOLDINGS;

    /** 정상 경합 결과인가(= 다음 후보로 계속 진행해도 되는가). */
    public boolean isRetriable() {
        return this == CANDIDATE_CONSUMED;
    }

    /** 비즈니스 오류인가(= 매칭을 중단해야 하는가). */
    public boolean isBusinessFailure() {
        return this == INSUFFICIENT_BALANCE || this == INSUFFICIENT_HOLDINGS;
    }
}
