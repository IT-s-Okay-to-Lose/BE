package com.example.iotl.global.response;

public interface BaseResponseService {
    /**
     * 성공 응답 메서드 - 전달 데이터 O
     *
     * @param data - 결과 데이터
     * @param <T>  - 반환 타입 => Generic
     * @return BaseResponse - 응답 객체
     */
    <T> BaseResponse<T> getSuccessResponse(T data);

    /**
     * 성공 응답 메서드 - 전달 데이터 X
     *
     * @param <T> - 반환 타입 => Generic
     * @return BaseResponse - 응답 객체
     */
    <T> BaseResponse<T> getSuccessResponse();

    /**
     * 실패 응답 메서드
     *
     * @param status - BaseResponseStatus에서 생성한 status
     * @param <T>    - 반환 타입 => Generic
     * @return BaseResponse - 응답 객체
     */
    <T> BaseResponse<T> getFailureResponse(BaseResponseStatus status);
}
