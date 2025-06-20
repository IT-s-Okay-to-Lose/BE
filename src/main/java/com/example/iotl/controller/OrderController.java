package com.example.iotl.controller;




import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.OrderRequestDto;
import com.example.iotl.dto.OrderResponseDto;
import com.example.iotl.entity.User;
import com.example.iotl.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto requestDto) {
        OrderResponseDto response = orderService.placeOrder(requestDto);
        return ResponseEntity.ok(response);
    }

    // 👉 주문 내역 조회 API
    @GetMapping("/history")
    public List<OrderHistoryDto> getOrderHistory(
        @RequestParam("userId") Long userId,
        @RequestParam("stockCode") String stockCode) {

        // 테스트용 코드: 실제 로그인 사용자 기반으로 바꾸는 게 좋음
        User user = new User(); // 👈 실제 로그인 사용자에서 가져와야 함
        user.setUserId(userId);

        return orderService.getOrderHistoryByUserAndStock(user, stockCode);
    }



}
