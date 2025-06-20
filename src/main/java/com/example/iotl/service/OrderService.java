package com.example.iotl.service;

import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.OrderRequestDto;
import com.example.iotl.dto.OrderResponseDto;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.StockRepository;
import com.example.iotl.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockInfoRepository stockInfoRepository;
    private final HoldingsRepository holdingsRepository;
    private final TradeService tradeService;
    private final OrderMatchingService orderMatchingService;

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
            .orderType(requestDto.getOrderType())
            .price(requestDto.getPrice())
            .quantity(requestDto.getQuantity())
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // (4) 저장
        orderRepository.save(order);

        //  (5) 체결 시도
        orderMatchingService.match(order);

        return OrderResponseDto.from(order);
    }


    public List<OrderHistoryDto> getOrderHistoryByUserAndStock(User user, String stockCode) {
        List<Order> orders = orderRepository.findByUserAndStock_StockCode(user, stockCode);
        return orders.stream()
            .map(OrderHistoryDto::from)
            .collect(Collectors.toList());
    }


//
//    private void handleBuyOrder(User user, Stocks stock, OrderRequestDto dto) {
//        // 가격과 수량 유효성 검사
//        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0 || dto.getQuantity() <= 0) {
//            throw new IllegalArgumentException("가격과 수량은 0보다 커야 합니다.");
//        }
//        // 총 주문 금액 = 가격 × 수량
//        BigDecimal totalCost = dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
//        BigDecimal currentBalance = user.getAccount().getBalance();
//
//        if (currentBalance.compareTo(totalCost) < 0) {
//            throw new IllegalStateException("예수금이 부족합니다.");
//        }
//
//        BigDecimal updatedBalance = currentBalance.subtract(totalCost);
//        user.getAccount().setBalance(updatedBalance.setScale(2, RoundingMode.HALF_UP));
//    }
//
//
//    private void handleSellOrder(User user, Stocks stock, OrderRequestDto dto) {
//        // (1) 보유 종목 조회
//
//        Holdings holding = holdingsRepository.findByUserAndStock(user, stock)
//            .orElseThrow(() -> new IllegalArgumentException("해당 종목을 보유하고 있지 않습니다."));
//
//        // (2) 보유 수량 >= 매도 수량 확인
//        if (holding.getQuantity() < dto.getQuantity()) {
//            throw new IllegalStateException("보유 수량이 부족합니다.");
//        }
//
//        // (3) 보유 수량 차감
//        holding.setQuantity(holding.getQuantity() - dto.getQuantity());
//
//        // (4) 매도 금액 계산 후 예수금 증가
//        BigDecimal sellAmount = dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
//        BigDecimal updatedBalance = user.getAccount().getBalance().add(sellAmount);
//
//        user.getAccount().setBalance(updatedBalance.setScale(2, RoundingMode.HALF_UP));
//    }
//


}
