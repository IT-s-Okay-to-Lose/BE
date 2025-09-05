package com.example.iotl.controller;

import com.example.iotl.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.iotl.service.security.TokenService;

import java.util.Map;

@RestController
@RequestMapping("/auth/test-auth")
@RequiredArgsConstructor
public class TestAuthController {

    private final TokenService tokenService;

    @GetMapping("/token")
    public BaseResponse<Map<String, String>> getTestToken() {
        String username = ""; // DB에 존재하는 username
        String role = "ROLE_USER";

        Map<String, String> tokenMap = tokenService.createTokens(username, role);

        return BaseResponse.<Map<String, String>>builder()
                .isSuccess(true)
                .message("테스트용 토큰 발급 완료")
                .code(200)
                .data(tokenMap)
                .build();
    }
}
