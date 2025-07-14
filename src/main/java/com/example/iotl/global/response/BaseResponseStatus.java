package com.example.iotl.global.response;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    // -------- 성공 코드 시작 -------- //
    SUCCESS(true, 1000, "요청에 성공했습니다."),
    // -------- 성공 코드 종료 -------- //

    // -------- 실패 코드 시작 -------- //
    // -------- 필요한 에러 코드 추가 => Code 만들 때 안겹치게 몇번대 사용할 건지 얘기할 것  -------- //
    /**
     * Member
     * Code : 2000번대
     */
    NOT_FOUND_MEMBER(false, 2001, "일치하는 사용자가 없습니다."),



    /**
     * Clothes
     * Code : 3000번대
     */
    NOT_FOUND_CLOTHES(false, 3001, "옷 정보가 존재하지 않습니다"),

                      /**
                       * Calendar
                       * Code : 4000번대
                       */

     NOT_FOUND_CALENDAR_INFO(false, 4001, "캘린더가 존재하지 않습니다.");

    // 기타 실패 코드


    // -------- 실패 코드 종료 -------- //

    private boolean isSuccess; // 성공 여부
    private String message; // 메시지
    private int code; // 코드

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
