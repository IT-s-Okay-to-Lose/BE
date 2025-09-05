package com.example.iotl.controller;

import com.example.iotl.dto.AccountSummaryDto;
import com.example.iotl.dto.deposit.DepositRequest;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.jwt.AuthenticationUtils;
import com.example.iotl.service.AccountService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final BaseResponseService baseResponseService;

    @PostMapping("/deposit")
    public String depositMoney(@RequestBody DepositRequest request) {
        String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            log.warn("❌ 인증되지 않은 사용자 요청");
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }

        BigDecimal amount = request.getAmount();
        log.info("💰 입금 요청: username={}, amount={}", username, amount);

        accountService.deposit(username, amount);
        return "💰 입금이 완료되었습니다.";
    }




    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<AccountSummaryDto>> getSummary() {
        String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401)
                .body(baseResponseService.getFailureResponse("인증된 사용자가 아닙니다.", 401));
        }

        AccountSummaryDto summary = accountService.getAccountSummary(username);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(summary));
    }


}
