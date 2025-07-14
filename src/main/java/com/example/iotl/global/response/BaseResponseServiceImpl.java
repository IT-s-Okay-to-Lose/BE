package com.example.iotl.global.response;


import org.springframework.stereotype.Component;

@Component
public class BaseResponseServiceImpl implements BaseResponseService {

    /**
     * 성공 응답 메서드 - 전달 데이터 O
     *
     * @param data - 결과 데이터
     * @param <T>  - 반환 타입 => Generic
     * @return BaseResponse - 응답 객체
     */
    @Override
    public <T> BaseResponse<T> getSuccessResponse(T data) {
        return BaseResponse.<T>builder()
                .isSuccess(true)
                .code(BaseResponseStatus.SUCCESS.getCode())
                .message(BaseResponseStatus.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * 성공 응답 메서드 - 전달 데이터 X
     *
     * @param <T> - 반환 타입 => Generic
     * @return BaseResponse - 응답 객체
     */
    @Override
    public <T> BaseResponse<T> getSuccessResponse() {
        return BaseResponse.<T>builder()
                .isSuccess(true)
                .code(BaseResponseStatus.SUCCESS.getCode())
                .message(BaseResponseStatus.SUCCESS.getMessage())
                .build();
    }

    /**
     * 실패 응답 메서드
     *
     * @param status - BaseResponseStatus에서 생성한 status
     * @param <T>    - 반환 타입 => Generic
     * @return BaseResponse - 응답 객체
     */
    @Override
    public <T> BaseResponse<T> getFailureResponse(BaseResponseStatus status) {
        return BaseResponse.<T>builder()
                .isSuccess(false)
                .code(status.getCode())
                .message(status.getMessage())
                .build();
    }
    @Override
    public <T> BaseResponse<T> getFailureResponse(String message, int code) {
        return BaseResponse.<T>builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
    @Override
    public <T> BaseResponse<T> getFailureResponse(String message, int code, T data) {
        return BaseResponse.<T>builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
