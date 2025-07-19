package com.example.iotl.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final BaseResponseService baseResponseService;

    public GlobalExceptionHandler(BaseResponseService baseResponseService) {
        this.baseResponseService = baseResponseService;
    }

    // 잘못된 파라미터 형식 (ex. year=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName(); // 파라미터 이름 (e.g. year)
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown";

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("parameter", name);
        errorDetail.put("invalidValue", value);
        errorDetail.put("expectedType", expectedType);

        return ResponseEntity
                .badRequest()
                .body(baseResponseService.getFailureResponse(
                        "요청 파라미터 형식이 잘못되었습니다.", 400, errorDetail));
    }

    // 그 외 모든 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseResponseService.getFailureResponse("서버 오류가 발생했습니다.", 500));
    }
}