package com.example.iotl.service;

import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.TradeDto;
import com.example.iotl.entity.*;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final HoldingsRepository holdingsRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public void trade(Order newOrder, Order matchedOrder) {
        // 1. 매수/매도 주문 판별
        Order buyOrder = (newOrder.getOrderType() == OrderType.BUY) ? newOrder : matchedOrder;
        Order sellOrder = (newOrder.getOrderType() == OrderType.SELL) ? newOrder : matchedOrder;

        // 2. 가격 & 최대 체결 수량 계산
        BigDecimal price = buyOrder.getPrice();  // 동일 가격 매칭 가정
        int maxQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

        // 3. 매도자의 실제 보유 수량 확인
        Holdings sellerHoldings = holdingsRepository.findByUserAndStock(sellOrder.getUser(),
                sellOrder.getStock())
            .orElseThrow(() -> new IllegalStateException("매도자의 주식 보유 정보가 없습니다."));

        // 4. 실제 체결 가능한 수량 계산
        int tradeQuantity = Math.min(maxQuantity, sellerHoldings.getQuantity());
        if (tradeQuantity <= 0) {
            throw new IllegalStateException("체결 가능한 수량이 없습니다.");
        }

        // 5. 매수자의 예수금 확인
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(tradeQuantity));
        if (buyOrder.getUser().getAccount().getBalance().compareTo(totalCost) < 0) {
            throw new IllegalStateException("매수자의 예수금이 부족합니다.");
        }

        // 6. 자산 이동
        buyOrder.getUser().getAccount().setBalance(
            buyOrder.getUser().getAccount().getBalance()
                .subtract(totalCost).setScale(2, RoundingMode.HALF_UP)
        );

        sellOrder.getUser().getAccount().setBalance(
            sellOrder.getUser().getAccount().getBalance()
                .add(totalCost).setScale(2, RoundingMode.HALF_UP)
        );

        // 7. 주식 이동
        sellerHoldings.setQuantity(sellerHoldings.getQuantity() - tradeQuantity);

        // 매수자 보유 종목 확인 or 신규 생성
        Holdings buyerHoldings = holdingsRepository.findByUserAndStock(buyOrder.getUser(),
                buyOrder.getStock())
            .orElse(
                Holdings.builder()
                    .user(buyOrder.getUser())
                    .stock(buyOrder.getStock())
                    .quantity(0)
                    .averageBuyPrice(BigDecimal.ZERO)
                    .build()
            );

        buyerHoldings.setQuantity(buyerHoldings.getQuantity() + tradeQuantity);

        holdingsRepository.save(sellerHoldings);
        holdingsRepository.save(buyerHoldings);

        // 8. 주문 수량 업데이트
        buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
        sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

        // 9. 주문 상태 업데이트
        buyOrder.setStatus(
            buyOrder.getQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);
        sellOrder.setStatus(
            sellOrder.getQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);

        // 10. 체결 기록 저장
        OrderType executedType = (buyOrder.equals(newOrder)) ? OrderType.BUY : OrderType.SELL;

        Trade tradeRecord = Trade.builder()
            .order(newOrder)  // 주체가 된 주문 저장
            .orderType(executedType)
            .executedPrice(price)
            .executedQuantity(tradeQuantity)
            .build();

        tradeRepository.save(tradeRecord);


    }

    public List<TradeDto> getTradesByUserAndStock(User user, String stockCode) {
        return tradeRepository.findByOrder_UserAndOrder_Stock_StockCode(user, stockCode)
            .stream()
            .map(TradeDto::from)
            .collect(Collectors.toList());
    }
}