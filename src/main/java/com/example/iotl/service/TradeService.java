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

    /**
     * [B2-4] CAS 우선 구조로 재설계한 체결.
     *
     * <p>1차 설계는 Holdings 검사(6단계)가 CAS(11단계)보다 먼저 실행되어,
     * S3 처럼 보유량이 빠듯한 상황에서 CAS 재조회가 <b>한 번도 작동하지 않았다</b>.
     *
     * <p>순서를 바꾼다:
     * <ol>
     *   <li>Order CAS 로 <b>해당 체결량에 대한 상태 전이 권한</b>을 먼저 확보</li>
     *   <li>0건이면 CANDIDATE_CONSUMED 반환 (예외 아님 → rollback-only 방지)</li>
     *   <li>CAS 성공 후에야 Holdings/Account 를 잠그고 실제 자산 검증</li>
     *   <li>자산 검증 실패 시 예외를 던져 같은 트랜잭션을 롤백 → CAS 변경도 원복</li>
     * </ol>
     */
    @Transactional
    public TradeResult trade(Order newOrder, Order matchedOrder) {
        Order buyOrder = (newOrder.getOrderType() == OrderType.BUY) ? newOrder : matchedOrder;
        Order sellOrder = (newOrder.getOrderType() == OrderType.SELL) ? newOrder : matchedOrder;

        BigDecimal price = buyOrder.getPrice();
        int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        if (tradeQuantity <= 0) {
            return TradeResult.CANDIDATE_CONSUMED;
        }

        // ── 1단계: Order CAS 로 상태 전이 권한을 먼저 확보한다.
        //    자산을 건드리기 전에 "이 체결량을 내가 가져간다"를 DB 가 원자적으로 판정.
        if (!consume(sellOrder, tradeQuantity) || !consume(buyOrder, tradeQuantity)) {
            return TradeResult.CANDIDATE_CONSUMED;
        }

        // ── 2단계: CAS 성공 이후에 자산을 잠그고 검증한다.
        Holdings sellerHoldings = holdingsRepository.findByUserAndStockWithPessimisticLock(
            sellOrder.getUser(), sellOrder.getStock()
        ).orElseThrow(() -> new IllegalStateException("매도자의 주식 보유 정보가 없습니다."));

        if (sellerHoldings.getQuantity() < tradeQuantity) {
            // CAS 는 성공했지만 실제 자산이 부족하다 -> 트랜잭션 롤백으로 CAS 도 원복된다.
            throw new InsufficientAssetException(
                TradeResult.INSUFFICIENT_HOLDINGS,
                "매도자 보유 수량 부족: 보유 " + sellerHoldings.getQuantity()
                    + ", 필요 " + tradeQuantity);
        }

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

        Accounts buyerAccount = accountsRepository.findByUserWithPessimisticLock(buyOrder.getUser())
            .orElseThrow(() -> new IllegalStateException("매수자 계좌 없음"));
        Accounts sellerAccount = accountsRepository.findByUserWithPessimisticLock(sellOrder.getUser())
            .orElseThrow(() -> new IllegalStateException("매도자 계좌 없음"));

        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(tradeQuantity));
        if (buyerAccount.getBalance().compareTo(totalCost) < 0) {
            throw new InsufficientAssetException(
                TradeResult.INSUFFICIENT_BALANCE,
                "매수자 예수금 부족: 잔액 " + buyerAccount.getBalance() + ", 필요 " + totalCost);
        }

        // ── 3단계: 자산 이동
        buyerAccount.setBalance(
            buyerAccount.getBalance().subtract(totalCost).setScale(2, RoundingMode.HALF_UP));
        sellerAccount.setBalance(
            sellerAccount.getBalance().add(totalCost).setScale(2, RoundingMode.HALF_UP));
        sellerAccount.setRealizedProfit(
            sellerAccount.getRealizedProfit().add(totalCost.setScale(2, RoundingMode.HALF_UP)));

        sellerHoldings.setQuantity(sellerHoldings.getQuantity() - tradeQuantity);
        buyerHoldings.setQuantity(buyerHoldings.getQuantity() + tradeQuantity);

        accountsRepository.save(buyerAccount);
        accountsRepository.save(sellerAccount);
        holdingsRepository.save(sellerHoldings);
        holdingsRepository.save(buyerHoldings);

        // ── 4단계: 체결 기록
        OrderType executedType = (buyOrder == newOrder) ? OrderType.BUY : OrderType.SELL;
        tradeRepository.save(Trade.builder()
            .order(newOrder)
            .orderType(executedType)
            .executedPrice(price)
            .executedQuantity(tradeQuantity)
            .build());

        return TradeResult.EXECUTED;
    }

    /**
     * [B2-1/B2-5] CAS 로 주문 잔량을 차감하고, 성공 시 메모리 인스턴스도 동기화한다.
     * @return 성공 여부. false = 다른 트랜잭션이 먼저 소비함(정상 경합).
     */
    private boolean consume(Order order, int tradeQuantity) {
        int expectedQty = order.getQuantity();
        OrderStatus newStatus =
            (expectedQty - tradeQuantity == 0) ? OrderStatus.COMPLETED : OrderStatus.PARTIAL;
        int affected = orderRepository.consumeQuantity(
            order.getId(), tradeQuantity, expectedQty, newStatus);
        if (affected == 0) {
            return false;
        }
        // [B2-5] 벌크 UPDATE 는 영속성 컨텍스트를 우회하므로
        // 호출자가 들고 있는 인스턴스를 DB 와 같은 값으로 맞춰준다.
        order.setQuantity(expectedQty - tradeQuantity);
        order.setStatus(newStatus);
        return true;
    }

    public List<TradeDto> getTradesByUserAndStock(User user, String stockCode) {
        return tradeRepository.findByOrder_UserAndOrder_Stock_StockCode(user, stockCode)
            .stream()
            .map(TradeDto::from)
            .collect(Collectors.toList());
    }
}
