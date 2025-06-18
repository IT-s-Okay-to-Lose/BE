package com.example.iotl.controller;




import com.example.iotl.dto.OrderRequestDto;
import com.example.iotl.dto.OrderResponseDto;
import com.example.iotl.service.OrderService;
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
}
