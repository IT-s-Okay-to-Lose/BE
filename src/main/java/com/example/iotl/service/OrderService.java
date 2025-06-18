package com.example.iotl.service;

import com.example.iotl.dto.OrderRequestDto;
import com.example.iotl.dto.OrderResponseDto;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.StockRepository;
import com.example.iotl.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockInfoRepository stockInfoRepository;

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {
        // (1) 유저 찾기
        User user = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // (2) 종목 찾기
        Stocks stocks = stockInfoRepository.findById(requestDto.getStockCode())
            .orElseThrow(() -> new IllegalArgumentException("해당 주식이 존재하지 않습니다."));

        // (3) Order 객체 생성
        Order order = Order.builder()
            .user(user)
            .stock(stocks)
            .orderType(requestDto.getOrderType()) // BUY or SELL
            .price(requestDto.getPrice())
            .quantity(requestDto.getQuantity()) // 수량
            .status(OrderStatus.valueOf("WAIT")) // 대기 상태로 기본 설정
            .createdAt(LocalDateTime.now())
            .build();

        // (4) 저장
        orderRepository.save(order);
        return OrderResponseDto.from(order);
    }


}
