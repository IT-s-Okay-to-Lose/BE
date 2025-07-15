package com.example.iotl.global.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BaseResponse<T> {

    private boolean isSuccess; // 성공, 실패 여부

    private String message; // 메시지

    private int code; // 코드

    private T data; // 전달 데이터

    @Builder
    public BaseResponse(boolean isSuccess, String message, int code, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.code = code;
        this.data = data;
    }
}