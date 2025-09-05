package com.example.iotl.controller;

import com.example.iotl.dto.holding.HoldingSummaryDto;
import com.example.iotl.dto.holding.MyStockSummaryDto;
import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.jwt.AuthenticationUtils;
import com.example.iotl.service.HoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
@Tag(name = "Holdings", description = "보유 주식 API")
public class HoldingController {

    private final HoldingService holdingService;
    private final BaseResponseService baseResponseService;


    @Operation(
        summary = "내 주식 요약 조회",
        description = "현재 로그인한 유저가 보유한 특정 종목의 평균가, 수수료, 수익 등을 반환합니다."
    )
    @GetMapping("/{stockCode}")
    @Operation(summary = "내 주식 요약 조회", description = "현재 로그인한 유저가 보유한 특정 종목의 평균가, 수수료, 수익을 반환합니다.")
    public ResponseEntity<BaseResponse<MyStockSummaryDto>> getMyStockSummary(@PathVariable String stockCode) {
        @Parameter(description = "종목 코드 (예: 005930)")
        @PathVariable String stockCode
  String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401)
                .body(baseResponseService.getFailureResponse("인증된 사용자가 아닙니다.", 401));
        }

        MyStockSummaryDto dto = holdingService.getMyStockSummary(username, stockCode);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(dto));


    @Operation(summary = "내 보유 종목 전체 요약 조회", description = "현재 로그인한 유저가 보유한 모든 종목의 요약 정보를 반환합니다.")
    @GetMapping("/summary")
    public BaseResponse<List<HoldingSummaryDto>> getMyHoldings(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        if (principal == null) {
            return baseResponseService.getFailureResponse("인증되지 않은 사용자입니다.", 401);
        }

        String username = principal.getUsername();
        List<HoldingSummaryDto> result = holdingService.getHoldingsByUserName(username);

        return baseResponseService.getSuccessResponse(result);

    }
}