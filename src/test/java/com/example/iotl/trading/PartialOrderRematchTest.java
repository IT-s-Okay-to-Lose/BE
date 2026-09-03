package com.example.iotl.trading;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iotl.dto.order.OrderRequestDto;
import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.repository.TradeRepository;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * [1단계] PARTIAL 주문이 이후 매칭 대상에서 누락되는 문제 재현.
 *
 * <p>근거가 되는 production 코드:
 * <ul>
 *   <li>TradeService.trade():100-103 — 잔량이 남으면 status 를 PARTIAL 로 바꾼다.</li>
 *   <li>OrderRepository.findMatchingOrders():18 — WHERE 절이 {@code o.status = 'PENDING'} 뿐이라
 *       PARTIAL 주문은 이후 어떤 신규 주문과도 매칭 후보가 되지 못한다.</li>
 * </ul>
 *
 * <p>이 테스트는 동시성이 전혀 없는 단일 스레드 시나리오다.
 * 즉 재현되면 "타이밍에 의존하는 이론적 위험"이 아니라 결정론적 로직 버그로 확정된다.
 */
@DisplayName("[1단계] PARTIAL 주문 재매칭 실패 재현")
class PartialOrderRematchTest extends TradingSliceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired TradeRepository tradeRepository;
    @Autowired HoldingsRepository holdingsRepository;
    @Autowired AccountsRepository accountsRepository;
    @Autowired UserRepository userRepository;
    @Autowired StocksRepository stocksRepository;

    private static final BigDecimal PRICE = new BigDecimal("1000");

    private User seller;
    private User buyer;
    private Stocks stock;

    @BeforeEach
    void setUp() {
        cleanUp();

        seller = userRepository.save(User.builder()
            .username("SELLER_" + System.nanoTime()).name("seller").role("USER")
            .email("seller_" + System.nanoTime() + "@test.com").build());
        buyer = userRepository.save(User.builder()
            .username("BUYER_" + System.nanoTime()).name("buyer").role("USER")
            .email("buyer_" + System.nanoTime() + "@test.com").build());

        accountsRepository.save(Accounts.builder()
            .user(seller).balance(new BigDecimal("0")).realizedProfit(BigDecimal.ZERO).build());
        accountsRepository.save(Accounts.builder()
            .user(buyer).balance(new BigDecimal("1000000")).realizedProfit(BigDecimal.ZERO).build());

        stock = stocksRepository.save(Stocks.builder()
            .stockCode("P" + (System.nanoTime() % 100000)).stockName("재매칭테스트").build());

        // 매도자는 총 10주 보유 (3주 + 7주 매도를 모두 소화할 수 있는 양)
        holdingsRepository.save(Holdings.builder()
            .user(seller).stock(stock).quantity(10).averageBuyPrice(PRICE).build());
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        tradeRepository.deleteAll();
        orderRepository.deleteAll();
        holdingsRepository.deleteAll();
        accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("PARTIAL 로 남은 BUY 주문은 이후 SELL 주문과 재매칭되지 않는다")
    void partialOrderIsNeverRematched() {
        // ── given: SELL 3주 (매칭 상대가 없으므로 PENDING 으로 대기)
        orderService.placeOrder(seller.getUsername(),
            orderRequest(OrderType.SELL, 3));

        // ── when 1: BUY 10주 → 위 SELL 3주와 부분 체결된다
        orderService.placeOrder(buyer.getUsername(),
            orderRequest(OrderType.BUY, 10));

        Order buyOrder = onlyOrderOf(buyer);
        Order firstSellOrder = sellOrders().get(0);

        // ── then 1: 3주가 체결되어 BUY 주문은 잔량 7주 / PARTIAL, SELL 주문은 COMPLETED
        assertThat(firstSellOrder.getStatus())
            .as("3주 매도 주문은 전량 체결되어 COMPLETED 여야 한다")
            .isEqualTo(OrderStatus.COMPLETED);
        assertThat(firstSellOrder.getQuantity())
            .as("체결 후 SELL 주문 잔량은 0 이어야 한다")
            .isEqualTo(0);

        assertThat(buyOrder.getStatus())
            .as("10주 중 3주만 체결되었으므로 BUY 주문은 PARTIAL 이어야 한다")
            .isEqualTo(OrderStatus.PARTIAL);
        assertThat(buyOrder.getQuantity())
            .as("10주 중 3주 체결 → 잔량 7주")
            .isEqualTo(7);

        assertThat(tradeRepository.count())
            .as("여기까지 체결 기록은 1건")
            .isEqualTo(1L);

        // ── when 2: SELL 7주 신규 주문
        // 정상이라면 위 PARTIAL BUY 주문(잔량 7주)과 체결되어 BUY 가 COMPLETED 가 되어야 한다.
        orderService.placeOrder(seller.getUsername(),
            orderRequest(OrderType.SELL, 7));

        Order buyOrderAfter = orderRepository.findById(buyOrder.getId()).orElseThrow();
        Order secondSellOrder = sellOrders().stream()
            .filter(o -> !o.getId().equals(firstSellOrder.getId()))
            .findFirst().orElseThrow();

        // ── then 2: 여기서 실패해야 한다 (= 버그 재현)
        assertThat(buyOrderAfter.getQuantity())
            .as("PARTIAL BUY 주문의 잔량 7주가 신규 SELL 7주와 체결되어 0이 되어야 한다")
            .isEqualTo(0);
        assertThat(buyOrderAfter.getStatus())
            .as("잔량이 모두 체결되었으므로 BUY 주문은 COMPLETED 여야 한다")
            .isEqualTo(OrderStatus.COMPLETED);
        assertThat(secondSellOrder.getStatus())
            .as("신규 SELL 7주도 전량 체결되어 COMPLETED 여야 한다")
            .isEqualTo(OrderStatus.COMPLETED);

        assertThat(tradeRepository.count())
            .as("총 체결 기록은 2건이어야 한다")
            .isEqualTo(2L);

        Holdings sellerHoldings =
            holdingsRepository.findByUserAndStock(seller, stock).orElseThrow();
        assertThat(sellerHoldings.getQuantity())
            .as("10주 보유에서 총 10주를 매도했으므로 0주여야 한다")
            .isEqualTo(0);
    }

    private OrderRequestDto orderRequest(OrderType type, int quantity) {
        OrderRequestDto dto = new OrderRequestDto();
        dto.setStockCode(stock.getStockCode());
        dto.setOrderType(type);
        dto.setPrice(PRICE);
        dto.setQuantity(quantity);
        return dto;
    }

    private Order onlyOrderOf(User user) {
        List<Order> orders =
            orderRepository.findByUserAndStock_StockCode(user, stock.getStockCode());
        assertThat(orders).hasSize(1);
        return orders.get(0);
    }

    private List<Order> sellOrders() {
        return orderRepository.findByUserAndStock_StockCode(seller, stock.getStockCode());
    }
}
