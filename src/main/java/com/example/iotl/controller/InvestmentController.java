package com.example.iotl.controller;

import com.example.iotl.dto.profit.ProfitResponse;
import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.entity.User;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/investments")
public class InvestmentController {
    private final BaseResponseService baseResponseService;
    private final UserRepository userRepository;
    private final InvestmentService investmentService;
    @GetMapping("/profits")
    public ResponseEntity<BaseResponse<ProfitResponse>> getProfitGraph(
            @RequestParam String period,
            @AuthenticationPrincipal CustomOAuth2User customUser
    ) {
        String username = customUser.getUsername();
        User user = userRepository.findOptionalByUsername(customUser.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        ProfitResponse response = investmentService.getRealizedProfitGraph(user, period);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(response));
    }
}
