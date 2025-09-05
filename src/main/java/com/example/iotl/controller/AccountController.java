package com.example.iotl.controller;

import com.example.iotl.dto.deposit.DepositRequest;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.jwt.AuthenticationUtils;
import com.example.iotl.service.AccountService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final BaseResponseService baseResponseService;

    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<String>> depositMoney(@RequestBody DepositRequest request) {
        String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            log.warn("❌ 인증되지 않은 사용자 요청");
            return ResponseEntity.status(401)
                .body(baseResponseService.getFailureResponse("인증된 사용자가 아닙니다.", 401));
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.signum() <= 0) {
            return ResponseEntity.badRequest()
                .body(baseResponseService.getFailureResponse("입금 금액이 올바르지 않습니다.", 400));
        }

        log.info("💰 입금 요청: username={}, amount={}", username, amount);
        accountService.deposit(username, amount);

        return ResponseEntity.ok(
            baseResponseService.getSuccessResponse("입금이 완료되었습니다.")
        );
    }
}
