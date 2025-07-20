package com.example.iotl.controller;

import com.example.iotl.dto.exchange.ExchangeSummaryDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.global.response.BaseResponseStatus;
import com.example.iotl.service.exchange.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/auth/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final BaseResponseService baseResponseService;

    @GetMapping
    public ResponseEntity<BaseResponse<ExchangeSummaryDto>> getExchangeSummary() {
        LocalDate today = LocalDate.now();

        Optional<ExchangeSummaryDto> result = exchangeService.getExchangeSummary(today);

        if (result.isPresent()) {
            return ResponseEntity.ok(baseResponseService.getSuccessResponse(result.get()));
        }

        // 없으면 저장 시도 후 다시 조회
        boolean saved = exchangeService.saveTodayExchangeIfAbsent();
        Optional<ExchangeSummaryDto> retried = exchangeService.getExchangeSummary(today);

        return retried.map(data ->
                        ResponseEntity.ok(baseResponseService.getSuccessResponse(data)))
                .orElseGet(() ->
                        ResponseEntity.status(404).body(
                                baseResponseService.getFailureResponse(
                                        "해당 날짜의 환율 정보를 찾을 수 없습니다.",
                                        404
                                )
                        ));
    }
}