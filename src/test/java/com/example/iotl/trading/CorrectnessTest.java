package com.example.iotl.trading;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iotl.dto.order.OrderRequestDto;
import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.Trade;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.repository.TradeRepository;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * [C1~C2] A2/B2 공통 정확성 테스트. 두 브랜치가 동일 파일을 공유한다.
 *
 * <p>deterministic correctness 만 검증한다. 성능 측정은 TradingBenchmarkTest 가 담당한다.
 */
@DisplayName("[정확성] C1~C2 공통 검증")
class CorrectnessTest extends TradingSliceTest {

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

    @BeforeEach
    void setUp() {
        cleanUp();
        stock = stocksRepository.save(Stocks.builder()
            .stockCode("C" + (System.nanoTime() % 100000)).stockName("정확성").build());
    }

    @AfterEach
    void tearDown() { cleanUp(); }

    private void cleanUp() {
        tradeRepository.deleteAll(); orderRepository.deleteAll();
        holdingsRepository.deleteAll(); accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─────────────────────────── C1 ───────────────────────────

    @Test
    @DisplayName("C1: SELL 1주에 BUY 1주 2건 동시 진입 -> 체결 정확히 1회")
    void c1_noDuplicateFill() throws Exception {
        User seller = userWithHoldings("C1S", 10);
        User buyerA = userWithBalance("C1A");
        User buyerB = userWithBalance("C1B");

        orderService.placeOrder(seller.getUsername(), req(OrderType.SELL, 1));
        Order sellOrder = orderRepository
            .findByUserAndStock_StockCode(seller, stock.getStockCode()).get(0);

        BigDecimal buyerBalanceBefore = balanceOf(buyerA);
        BigDecimal sellerBalanceBefore = balanceOf(seller);

        CyclicBarrier barrier = new CyclicBarrier(2);
        CountDownLatch done = new CountDownLatch(2);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        for (User buyer : List.of(buyerA, buyerB)) {
            pool.submit(() -> {
                try {
                    barrier.await(10, TimeUnit.SECONDS);
                    orderService.placeOrder(buyer.getUsername(), req(OrderType.BUY, 1));
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        List<Trade> trades = tradeRepository.findAll();
        Holdings sh = holdingsRepository.findByUserAndStock(seller, stock).orElseThrow();
        Order after = orderRepository.findById(sellOrder.getId()).orElseThrow();

        int executedSum = trades.stream().mapToInt(Trade::getExecutedQuantity).sum();
        int originalQty = 1;                       // SELL 주문의 최초 수량
        int overfill = Math.max(0, executedSum - originalQty);

        int deadlocks = 0;
        int lockTimeouts = 0;
        java.util.Map<String, Integer> failKinds = new java.util.LinkedHashMap<>();
        for (Throwable t : failures) {
            if (BenchmarkSupport.isDeadlock(t)) {
                deadlocks++;
                failKinds.merge("deadlock(1213)", 1, Integer::sum);
            } else if (BenchmarkSupport.isLockWaitTimeout(t)) {
                lockTimeouts++;
                failKinds.merge("lockTimeout(1205)", 1, Integer::sum);
            } else {
                failKinds.merge(t.getClass().getSimpleName(), 1, Integer::sum);
            }
        }

        // invariant 위반 수를 개별로 센다. assert 이전에 세어야 첫 실패에서 멈추지 않는다.
        int violations = 0;
        if (trades.size() != 1) violations++;
        if (executedSum != 1) violations++;
        if (overfill != 0) violations++;
        if (sh.getQuantity() != 9) violations++;
        if (after.getStatus() != OrderStatus.COMPLETED) violations++;
        if (after.getQuantity() != 0) violations++;

        java.util.Map<String, String> rec = RunContext.base("C1", 2, 1);
        rec.put("total_requests", "2");
        rec.put("success", String.valueOf(2 - failures.size()));
        rec.put("failure", String.valueOf(failures.size()));
        rec.put("failure_kinds", failKinds.isEmpty() ? "none" : failKinds.toString());
        rec.put("original_order_quantity", String.valueOf(originalQty));
        rec.put("executed_quantity_sum", String.valueOf(executedSum));
        rec.put("overfill_quantity", String.valueOf(overfill));
        rec.put("order_status", after.getStatus().name());
        rec.put("order_remaining_quantity", String.valueOf(after.getQuantity()));
        rec.put("holdings_before", "10");
        rec.put("holdings_after", String.valueOf(sh.getQuantity()));
        rec.put("account_before", buyerBalanceBefore.toPlainString()
            + "|" + sellerBalanceBefore.toPlainString());
        rec.put("account_after", balanceOf(buyerA).toPlainString()
            + "|" + balanceOf(buyerB).toPlainString()
            + "|" + balanceOf(seller).toPlainString());
        rec.put("invariant_violations", String.valueOf(violations));
        rec.put("deadlocks", String.valueOf(deadlocks));
        rec.put("lock_timeouts", String.valueOf(lockTimeouts));
        RunContext.append("correctness.csv", rec);

        assertThat(trades).as("체결은 정확히 1건").hasSize(1);
        assertThat(trades.stream().mapToInt(Trade::getExecutedQuantity).sum())
            .as("체결 수량 합계는 1").isEqualTo(1);
        assertThat(sh.getQuantity()).as("seller holdings = 9").isEqualTo(9);
        assertThat(after.getStatus()).as("SELL = COMPLETED").isEqualTo(OrderStatus.COMPLETED);
        assertThat(after.getQuantity()).as("SELL 잔량 = 0").isEqualTo(0);
    }

    // ─────────────────────────── C2 ───────────────────────────

    @Test
    @DisplayName("C2: BUY 100 에 SELL 30->20->10->40 순차 체결 -> 잔량 70/50/40/0")
    void c2_partialChain() {
        User buyer = userWithBalance("C2B");
        User seller = userWithHoldings("C2S", 200);

        orderService.placeOrder(buyer.getUsername(), req(OrderType.BUY, 100));
        Order buyOrder = orderRepository
            .findByUserAndStock_StockCode(buyer, stock.getStockCode()).get(0);

        int[] sells = {30, 20, 10, 40};
        int[] expectedRemaining = {70, 50, 40, 0};
        List<String> log = new ArrayList<>();

        for (int i = 0; i < sells.length; i++) {
            orderService.placeOrder(seller.getUsername(), req(OrderType.SELL, sells[i]));
            Order cur = orderRepository.findById(buyOrder.getId()).orElseThrow();
            log.add(String.format("SELL %-3d -> BUY 잔량 %-3d (기대 %d) / 상태 %s",
                sells[i], cur.getQuantity(), expectedRemaining[i], cur.getStatus()));
            assertThat(cur.getQuantity())
                .as("[%s] SELL %d 이후 BUY 잔량", VARIANT, sells[i])
                .isEqualTo(expectedRemaining[i]);
        }

        Order finalBuy = orderRepository.findById(buyOrder.getId()).orElseThrow();
        int sum = tradeRepository.findAll().stream().mapToInt(Trade::getExecutedQuantity).sum();

        int overfill = Math.max(0, sum - 100);
        int violations = 0;
        if (finalBuy.getStatus() != OrderStatus.COMPLETED) violations++;
        if (finalBuy.getQuantity() != 0) violations++;
        if (sum != 100) violations++;
        if (overfill != 0) violations++;

        java.util.Map<String, String> rec = RunContext.base("C2", 1, 1);
        rec.put("total_requests", "5");           // BUY 1건 + SELL 4건
        rec.put("success", "5");
        rec.put("failure", "0");
        rec.put("failure_kinds", "none");
        rec.put("original_order_quantity", "100");
        rec.put("executed_quantity_sum", String.valueOf(sum));
        rec.put("overfill_quantity", String.valueOf(overfill));
        rec.put("order_status", finalBuy.getStatus().name());
        rec.put("order_remaining_quantity", String.valueOf(finalBuy.getQuantity()));
        rec.put("holdings_before", "200");
        rec.put("holdings_after", String.valueOf(
            holdingsRepository.findByUserAndStock(seller, stock).orElseThrow().getQuantity()));
        rec.put("account_before", "N/A");         // C2 는 잔액 불변식을 검증 대상으로 삼지 않음
        rec.put("account_after", "N/A");
        rec.put("invariant_violations", String.valueOf(violations));
        rec.put("deadlocks", "0");
        rec.put("lock_timeouts", "0");
        rec.put("partial_chain", String.join(" | ", log).replace(",", ";"));
        RunContext.append("correctness.csv", rec);

        System.out.println("\n===== [C2] PARTIAL 체인 (" + VARIANT + ") =====");
        log.forEach(System.out::println);
        rec.forEach((k, v) -> System.out.printf("  %-28s %s%n", k, v));
        System.out.println("=========================================\n");

        assertThat(finalBuy.getStatus()).as("최종 COMPLETED").isEqualTo(OrderStatus.COMPLETED);
        assertThat(sum).as("체결 수량 합계 = 100").isEqualTo(100);
    }

    // ─────────────────────────── helpers ───────────────────────────

    private BigDecimal balanceOf(User u) {
        return accountsRepository.findAll().stream()
            .filter(a -> a.getUser().getUserId().equals(u.getUserId()))
            .findFirst().orElseThrow().getBalance();
    }

    private User userWithBalance(String prefix) {
        User u = saveUser(prefix);
        accountsRepository.save(Accounts.builder().user(u)
            .balance(new BigDecimal("100000000.00")).realizedProfit(BigDecimal.ZERO).build());
        return u;
    }

    private User userWithHoldings(String prefix, int qty) {
        User u = saveUser(prefix);
        accountsRepository.save(Accounts.builder().user(u)
            .balance(new BigDecimal("100000000.00")).realizedProfit(BigDecimal.ZERO).build());
        holdingsRepository.save(Holdings.builder().user(u).stock(stock)
            .quantity(qty).averageBuyPrice(PRICE).build());
        return u;
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

    private static String shortMsg(Throwable t) {
        String m = t.getMessage();
        if (m == null) return "(no message)";
        m = m.replaceAll("\\s+", " ");
        return m.length() > 100 ? m.substring(0, 100) + "..." : m;
    }
}
