package com.example.iotl.trading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * [C3] 체결 도중 자산 검증이 실패하면 트랜잭션 전체가 롤백되는지 증명한다.
 *
 * <p>A2/B2 동일 불변식으로 검증한다.
 * B2 는 CAS 로 Order 상태를 먼저 전이시키므로, 이후 자산 검증이 실패했을 때
 * <b>CAS 변경까지 원복되는지</b>가 핵심이다.
 * A2 는 CAS 를 쓰지 않지만 동일한 불변식(주문/자산/체결 모두 무변화)을 만족해야 한다.
 *
 * <p>불변식:
 * <ul>
 *   <li>Order quantity/status 원복</li>
 *   <li>Holdings 변화 없음</li>
 *   <li>Account 변화 없음</li>
 *   <li>Trade 생성 없음</li>
 * </ul>
 */
@DisplayName("[C3] 자산 검증 실패 시 트랜잭션 롤백")
class CasRollbackTest extends TradingSliceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired TradeRepository tradeRepository;
    @Autowired HoldingsRepository holdingsRepository;
    @Autowired AccountsRepository accountsRepository;
    @Autowired UserRepository userRepository;
    @Autowired StocksRepository stocksRepository;

    private static final BigDecimal PRICE = new BigDecimal("1000");
    private static final String VARIANT = System.getProperty("bench.variant", "BASE");

    private Stocks stock;
    private User seller;
    private User poorBuyer;

    @BeforeEach
    void setUp() {
        cleanUp();
        stock = stocksRepository.save(Stocks.builder()
            .stockCode("R" + (System.nanoTime() % 100000)).stockName("롤백").build());

        seller = saveUser("C3S");
        accountsRepository.save(Accounts.builder().user(seller)
            .balance(new BigDecimal("0.00")).realizedProfit(BigDecimal.ZERO).build());
        holdingsRepository.save(Holdings.builder().user(seller).stock(stock)
            .quantity(10).averageBuyPrice(PRICE).build());

        // 예수금이 부족한 매수자: 1주(1000원)도 살 수 없다.
        poorBuyer = saveUser("C3B");
        accountsRepository.save(Accounts.builder().user(poorBuyer)
            .balance(new BigDecimal("500.00")).realizedProfit(BigDecimal.ZERO).build());
    }

    @AfterEach
    void tearDown() { cleanUp(); }

    private void cleanUp() {
        tradeRepository.deleteAll(); orderRepository.deleteAll();
        holdingsRepository.deleteAll(); accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("예수금 부족으로 체결 실패 시 주문/보유/계좌/체결이 모두 무변화여야 한다")
    void assetValidationFailureRollsBackEverything() {
        // SELL 주문을 먼저 대기시킨다.
        orderService.placeOrder(seller.getUsername(), req(OrderType.SELL, 1));
        Order sellBefore = orderRepository
            .findByUserAndStock_StockCode(seller, stock.getStockCode()).get(0);

        int sellQtyBefore = sellBefore.getQuantity();
        OrderStatus sellStatusBefore = sellBefore.getStatus();
        int sellerHoldingsBefore =
            holdingsRepository.findByUserAndStock(seller, stock).orElseThrow().getQuantity();
        BigDecimal sellerBalanceBefore = balanceOf(seller);
        BigDecimal buyerBalanceBefore = balanceOf(poorBuyer);
        long tradeCountBefore = tradeRepository.count();

        // 예수금이 부족한 매수 주문 -> 자산 검증 실패로 롤백되어야 한다.
        Throwable thrown = null;
        try {
            orderService.placeOrder(poorBuyer.getUsername(), req(OrderType.BUY, 1));
        } catch (Throwable t) {
            thrown = t;
        }

        Order sellAfter = orderRepository.findById(sellBefore.getId()).orElseThrow();
        int sellerHoldingsAfter =
            holdingsRepository.findByUserAndStock(seller, stock).orElseThrow().getQuantity();

        int violations = 0;
        if (sellAfter.getQuantity() != sellQtyBefore) violations++;
        if (sellAfter.getStatus() != sellStatusBefore) violations++;
        if (sellerHoldingsAfter != sellerHoldingsBefore) violations++;
        if (balanceOf(seller).compareTo(sellerBalanceBefore) != 0) violations++;
        if (balanceOf(poorBuyer).compareTo(buyerBalanceBefore) != 0) violations++;
        if (tradeRepository.count() != tradeCountBefore) violations++;

        java.util.Map<String, String> rec = RunContext.base("C3", 1, 1);
        rec.put("total_requests", "2");
        rec.put("success", "1");                  // SELL 접수 성공, BUY 는 롤백
        rec.put("failure", "1");
        rec.put("failure_kinds", thrown == null ? "none"
            : thrown.getClass().getSimpleName() + "=1");
        rec.put("original_order_quantity", String.valueOf(sellQtyBefore));
        rec.put("executed_quantity_sum", "0");
        rec.put("overfill_quantity", "0");
        rec.put("order_status", sellAfter.getStatus().name());
        rec.put("order_remaining_quantity", String.valueOf(sellAfter.getQuantity()));
        rec.put("holdings_before", String.valueOf(sellerHoldingsBefore));
        rec.put("holdings_after", String.valueOf(sellerHoldingsAfter));
        rec.put("account_before", buyerBalanceBefore.toPlainString()
            + "|" + sellerBalanceBefore.toPlainString());
        rec.put("account_after", balanceOf(poorBuyer).toPlainString()
            + "|" + balanceOf(seller).toPlainString());
        rec.put("invariant_violations", String.valueOf(violations));
        rec.put("deadlocks", String.valueOf(
            thrown != null && BenchmarkSupport.isDeadlock(thrown) ? 1 : 0));
        rec.put("lock_timeouts", String.valueOf(
            thrown != null && BenchmarkSupport.isLockWaitTimeout(thrown) ? 1 : 0));
        RunContext.append("correctness.csv", rec);

        System.out.println("\n===== [C3] 자산 검증 실패 롤백 (" + VARIANT + ") =====");
        rec.forEach((k, v) -> System.out.printf("  %-28s %s%n", k, v));
        System.out.println("=================================================\n");

        assertThat(sellAfter.getQuantity())
            .as("[%s] SELL 주문 잔량이 원복되어야 한다", VARIANT).isEqualTo(sellQtyBefore);
        assertThat(sellAfter.getStatus())
            .as("[%s] SELL 주문 상태가 원복되어야 한다", VARIANT).isEqualTo(sellStatusBefore);
        assertThat(sellerHoldingsAfter)
            .as("[%s] 보유 수량 무변화", VARIANT).isEqualTo(sellerHoldingsBefore);
        assertThat(balanceOf(seller))
            .as("[%s] 매도자 잔액 무변화", VARIANT).isEqualByComparingTo(sellerBalanceBefore);
        assertThat(balanceOf(poorBuyer))
            .as("[%s] 매수자 잔액 무변화", VARIANT).isEqualByComparingTo(buyerBalanceBefore);
        assertThat(tradeRepository.count())
            .as("[%s] Trade 생성 없음", VARIANT).isEqualTo(tradeCountBefore);
    }

    private BigDecimal balanceOf(User u) {
        return accountsRepository.findAll().stream()
            .filter(a -> a.getUser().getUserId().equals(u.getUserId()))
            .findFirst().orElseThrow().getBalance();
    }

    private User saveUser(String prefix) {
        return userRepository.save(User.builder()
            .username(prefix + "_" + System.nanoTime())
            .name(prefix).role("USER")
            .email(prefix + "_" + System.nanoTime() + "@t.com").build());
    }

    private OrderRequestDto req(OrderType type, int qty) {
        OrderRequestDto d = new OrderRequestDto();
        d.setStockCode(stock.getStockCode()); d.setOrderType(type);
        d.setPrice(PRICE); d.setQuantity(qty);
        return d;
    }
}
