package com.example.iotl.controller;

import com.example.iotl.dto.deposit.DepositRequest;
import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/deposit")
    public String depositMoney(
        @AuthenticationPrincipal CustomOAuth2User principal,
        @RequestBody DepositRequest request) {

        String username = principal.getUsername(); // OAuth2 로그인한 사용자 이메일 or username
        BigDecimal amount = request.getAmount();

        accountService.deposit(username, amount);
        return "💰 입금이 완료되었습니다.";
    }
}
