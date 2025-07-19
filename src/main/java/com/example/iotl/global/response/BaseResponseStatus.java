package com.example.iotl.global.response;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {
    SUCCESS(true, 200, "요청에 성공했습니다."),
    // -------- 인증/인가 오류 (1100~1199) -------- //
    UNAUTHORIZED(false, 400, "인증이 필요합니다."),
    FORBIDDEN(false, 400, "접근 권한이 없습니다."),
    INVALID_TOKEN(false, 400, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(false, 400, "토큰이 만료되었습니다."),

    // // -------- 대시보드/보유 종목 (3000~3099) -------- //
    // NOT_FOUND_HOLDINGS(false, 3000, "보유 종목 정보가 없습니다."),
    // INVALID_HOLDING_RATIO_REQUEST(false, 3001, "보유 종목 요청이 잘못되었습니다."),
    //
    // // -------- 뉴스/외부 API 오류 (3100~3199) -------- //
    // NEWS_API_ERROR(false, 3100, "뉴스 API 호출에 실패했습니다."),
    // EMPTY_NEWS_RESULT(false, 3101, "조회된 뉴스가 없습니다."),
    //
    // // -------- 실현 수익/투자 요약 관련 (3200~3299) -------- //
    // INVALID_DATE_REQUEST(false, 3200, "year 또는 month가 누락되었거나 잘못된 형식입니다."),
    // NOT_FOUND_REALIZED_PROFIT(false, 3201, "실현 수익 정보가 없습니다."),

    // -------- 서버 오류 (5000~5999) -------- //
    INTERNAL_SERVER_ERROR(false, 500, "서버 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(false, 501, "일시적으로 서비스를 사용할 수 없습니다.");


    private final boolean isSuccess; // 성공 여부
    private final int code; // 코드
    private final String message; // 메시지

    /**
     * BaseResponseStatus 에서 해당하는 코드를 매핑
     *
     * @param isSuccess
     * @param code
     * @param message
     */
    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
