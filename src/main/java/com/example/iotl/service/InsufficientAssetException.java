package com.example.iotl.service;

import lombok.Getter;

/**
 * [B2-1] 자산 부족으로 체결할 수 없을 때 던진다.
 *
 * <p>CANDIDATE_CONSUMED(정상 경합)와 달리 이것은 <b>비즈니스 오류</b>다.
 * CAS 로 확보한 상태 전이를 되돌려야 하므로 예외로 던져 트랜잭션을 롤백시킨다.
 * 이 롤백은 의도된 것이며, rollback-only 문제와 성격이 다르다.
 */
@Getter
public class InsufficientAssetException extends RuntimeException {

    private final TradeResult result;

    public InsufficientAssetException(TradeResult result, String message) {
        super(message);
        this.result = result;
    }
}
