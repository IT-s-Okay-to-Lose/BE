package com.example.iotl.controller;





import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.order.OrderRequestDto;
import com.example.iotl.dto.order.OrderResponseDto;

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

        log.info("💡 /orders 호출됨");
        OrderResponseDto response = orderService.placeOrder(requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public List<OrderHistoryDto> getOrderHistory(
        @AuthenticationPrincipal CustomOAuth2User principal,
        @RequestParam("stockCode") String stockCode) {

//        log.info("💡 /history 호출됨");
//        log.info("컨트롤러 진입 확인 ✅");

//        if (principal == null) {
//            log.warn("❗ principal이 null입니다.");
//            return List.of();
//        }


        // OAuth 로그인된 사용자 정보에서 username 추출
        String username = principal.getUsername();
//        log.info("로그인한 사용자 username: {}", username);


        return orderService.getOrderHistoryByUsernameAndStock(username, stockCode);
    }




}
