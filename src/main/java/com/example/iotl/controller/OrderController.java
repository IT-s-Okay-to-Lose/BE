package com.example.iotl.controller;





import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.order.OrderRequestDto;
import com.example.iotl.dto.order.OrderResponseDto;

import com.example.iotl.jwt.AuthenticationUtils;
import com.example.iotl.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto requestDto) {
        String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }

        OrderResponseDto response = orderService.placeOrder(username, requestDto);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/history")
    public List<OrderHistoryDto> getOrderHistory(@RequestParam("stockCode") String stockCode) {
        String username = AuthenticationUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }

        return orderService.getOrderHistoryByUsernameAndStock(username, stockCode);
    }




}
