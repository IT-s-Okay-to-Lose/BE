package com.example.iotl.service;

import com.example.iotl.dto.TradeDto;
import com.example.iotl.entity.*;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.TradeRepository;
import com.example.iotl.repository.AccountsRepository;
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
    private final AccountsRepository accountsRepository;

    @Transactional
    public void trade(Order newOrder, Order matchedOrder) {
        // 1. 매수/매도 주문 판별
        Order buyOrder = (newOrder.getOrderType() == OrderType.BUY) ? newOrder : matchedOrder;
        Order sellOrder = (newOrder.getOrderType() == OrderType.SELL) ? newOrder : matchedOrder;

        // 2. 가격 & 최대 체결 수량 계산
        BigDecimal price = buyOrder.getPrice();  // 동일 가격 매칭 가정
        int maxQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

        // 3. 매도자의 보유 주식 비관적 락 조회
        Holdings sellerHoldings = holdingsRepository.findByUserAndStockWithPessimisticLock(
            sellOrder.getUser(), sellOrder.getStock()
        ).orElseThrow(() -> new IllegalStateException("매도자의 주식 보유 정보가 없습니다."));

        // 4. 매수자의 보유 주식 비관적 락 조회 (있으면 가져오고, 없으면 새로 만듦)
        Holdings buyerHoldings = holdingsRepository.findByUserAndStockWithPessimisticLock(
            buyOrder.getUser(), buyOrder.getStock()
        ).orElse(
            Holdings.builder()
                .user(buyOrder.getUser())
                .stock(buyOrder.getStock())
                .quantity(0)
                .averageBuyPrice(BigDecimal.ZERO)
                .build()
        );

        // 5. 매수/매도자 계좌 비관적 락 조회
        Accounts buyerAccount = accountsRepository.findByUserWithPessimisticLock(buyOrder.getUser())
            .orElseThrow(() -> new IllegalStateException("매수자 계좌 없음"));
        Accounts sellerAccount = accountsRepository.findByUserWithPessimisticLock(sellOrder.getUser())
            .orElseThrow(() -> new IllegalStateException("매도자 계좌 없음"));

        // 6. 실제 체결 가능한 수량 계산
        int tradeQuantity = Math.min(maxQuantity, sellerHoldings.getQuantity());
        if (tradeQuantity <= 0) {
            throw new IllegalStateException("체결 가능한 수량이 없습니다.");
        }

        // 7. 매수자의 예수금 확인
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(tradeQuantity));
        if (buyerAccount.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalStateException("매수자의 예수금이 부족합니다.");
        }

        // 8. 계좌 잔고 이동
        buyerAccount.setBalance(
            buyerAccount.getBalance().subtract(totalCost).setScale(2, RoundingMode.HALF_UP)
        );
        sellerAccount.setBalance(
            sellerAccount.getBalance().add(totalCost).setScale(2, RoundingMode.HALF_UP)
        );

        // 9. 주식 이동
        sellerHoldings.setQuantity(sellerHoldings.getQuantity() - tradeQuantity);
        buyerHoldings.setQuantity(buyerHoldings.getQuantity() + tradeQuantity);

        // 10. 엔티티 저장
        accountsRepository.save(buyerAccount);
        accountsRepository.save(sellerAccount);
        holdingsRepository.save(sellerHoldings);
        holdingsRepository.save(buyerHoldings);

        // 11. 주문 수량 및 상태 업데이트
        buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
        sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

        buyOrder.setStatus(
            buyOrder.getQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);
        sellOrder.setStatus(
            sellOrder.getQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);

        // 12. 체결 기록 저장
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
