package com.example.iotl.controller;

import com.example.iotl.dto.TradeDto;
import com.example.iotl.entity.User;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.jwt.AuthenticationUtils;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
    private final UserRepository userRepository;
    private final TradeService tradeService;
    private final BaseResponseService baseResponseService;

    /**
     * (권장) 로그인 사용자 체결내역 조회 - 경로변수 버전
     * GET /api/trades/{stockCode}
     */
    @GetMapping("/{stockCode}")
    @Operation(summary = "로그인 사용자 체결내역 조회 (경로변수)")
    public ResponseEntity<BaseResponse<List<TradeDto>>> getTradesByPath(@PathVariable String stockCode) {
        String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401)
                .body(baseResponseService.getFailureResponse("인증된 사용자가 아닙니다.", 401));
        }

        Optional<User> userOpt = Optional.ofNullable(userRepository.findByUsername(username));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(baseResponseService.getFailureResponse("사용자를 찾을 수 없습니다.", 404));
        }

        List<TradeDto> data = tradeService.getTradesByUserAndStock(userOpt.get(), stockCode);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }
}
