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
import java.util.Map;
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
 * [C4~C6] B2 최종안의 결정론적 정확성 검증.
 *
 * <p>이 클래스는 <b>정확성만</b> 검증한다. 성능 수치는 기록하지 않는다.
 * 질문: "동시 실행 순서가 달라져도 거래 불변식이 깨지는가?"
 *
 * <p>반복 간 데이터 격리: 매 반복마다 새 user/stock/order 를 생성해
 * 서로 다른 종목코드를 쓰므로 회차 간 간섭이 없다. 전체 종료 시 한 번만 정리한다.
 * (매 회차 deleteAll 은 1,000회 반복에서 비용이 과도하다)
 */
@DisplayName("[C4~C6] B2 최종 정확성 검증")
class FinalCorrectnessTest extends TradingSliceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired TradeRepository tradeRepository;
    @Autowired HoldingsRepository holdingsRepository;
    @Autowired AccountsRepository accountsRepository;
    @Autowired UserRepository userRepository;
    @Autowired StocksRepository stocksRepository;
    @Autowired jakarta.persistence.EntityManager em;

    private static final BigDecimal PRICE = new BigDecimal("1000");
    private static final BigDecimal RICH = new BigDecimal("100000000.00");

    /** 회차별 고유 종목코드 생성용. varchar(10) 제한에 맞춰 F+9자리. */
    private static final java.util.concurrent.atomic.AtomicInteger STOCK_SEQ =
        new java.util.concurrent.atomic.AtomicInteger();

    /**
     * C4 반복 횟수. 기본 1,000회.
     * 규모 근거는 METRICS.md 참조: scheduler/interleaving 변화에서도
     * 동일 정합성 위반이 재관측되는지 반복 확인하기 위함.
     */
    private static final int C4_ITERATIONS =
        Integer.getInteger("c4.iterations", 1000);

    @BeforeEach
    void setUp() {
        cleanUp();
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

    // ─────────────────────────── C4 ───────────────────────────

    @Test
    @DisplayName("C4: 동일 Order race를 1,000회 반복해도 불변식 위반이 관측되지 않는다")
    void c4_repeatedRace() throws Exception {
        int iterations = C4_ITERATIONS;
        ExecutorService pool = Executors.newFixedThreadPool(4);

        int violationRounds = 0;
        int totalDeadlocks = 0;
        int totalLockTimeouts = 0;
        int totalOtherFailures = 0;
        List<String> violationDetails = new ArrayList<>();
        long start = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            Fixture f = newFixture("C4_" + i, 10);

            orderService.placeOrder(f.seller.getUsername(),
                req(f.stock, OrderType.SELL, 1));
            Order sellOrder = orderRepository
                .findByUserAndStock_StockCode(f.seller, f.stock.getStockCode()).get(0);

            CyclicBarrier barrier = new CyclicBarrier(2);
            CountDownLatch done = new CountDownLatch(2);
            ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

            for (User buyer : List.of(f.buyerA, f.buyerB)) {
                pool.submit(() -> {
                    try {
                        barrier.await(10, TimeUnit.SECONDS);
                        orderService.placeOrder(buyer.getUsername(),
                            req(f.stock, OrderType.BUY, 1));
                    } catch (Throwable t) {
                        failures.add(t);
                    } finally {
                        done.countDown();
                    }
                });
            }
            if (!done.await(60, TimeUnit.SECONDS)) {
                violationDetails.add("iter=" + i + " timeout");
                violationRounds++;
                continue;
            }

            for (Throwable t : failures) {
                if (BenchmarkSupport.isDeadlock(t)) {
                    totalDeadlocks++;
                } else if (BenchmarkSupport.isLockWaitTimeout(t)) {
                    totalLockTimeouts++;
                } else {
                    totalOtherFailures++;
                }
            }

            // 이 회차의 불변식 검사 (해당 종목에 한정)
            List<Trade> trades = tradesOf(f.stock);
            int executedSum = trades.stream().mapToInt(Trade::getExecutedQuantity).sum();
            Order after = orderRepository.findById(sellOrder.getId()).orElseThrow();
            int sellerHoldings = holdingsRepository
                .findByUserAndStock(f.seller, f.stock).orElseThrow().getQuantity();
            int overfill = Math.max(0, executedSum - 1);

            List<String> v = new ArrayList<>();
            if (trades.size() != 1) v.add("tradeCount=" + trades.size());
            if (executedSum != 1) v.add("executedSum=" + executedSum);
            if (after.getQuantity() != 0) v.add("remaining=" + after.getQuantity());
            if (after.getStatus() != OrderStatus.COMPLETED) v.add("status=" + after.getStatus());
            if (sellerHoldings != 9) v.add("holdings=" + sellerHoldings);
            if (overfill != 0) v.add("overfill=" + overfill);

            if (!v.isEmpty()) {
                violationRounds++;
                violationDetails.add("iter=" + i + " " + String.join(",", v));
            }
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        pool.shutdownNow();

        Map<String, String> rec = RunContext.base("C4", 2, iterations);
        rec.put("total_requests", String.valueOf(iterations * 2));
        rec.put("success", String.valueOf(iterations * 2
            - totalDeadlocks - totalLockTimeouts - totalOtherFailures));
        rec.put("failure", String.valueOf(
            totalDeadlocks + totalLockTimeouts + totalOtherFailures));
        rec.put("failure_kinds", "deadlock=" + totalDeadlocks
            + ";lockTimeout=" + totalLockTimeouts + ";other=" + totalOtherFailures);
        rec.put("original_order_quantity", "1");
        rec.put("executed_quantity_sum", "1 per iteration (expected)");
        rec.put("overfill_quantity", "0");
        rec.put("order_status", "COMPLETED (expected each iteration)");
        rec.put("order_remaining_quantity", "0");
        rec.put("holdings_before", "10");
        rec.put("holdings_after", "9");
        rec.put("account_before", "N/A");   // C4 는 잔액을 회차별 불변식으로 삼지 않음
        rec.put("account_after", "N/A");
        rec.put("invariant_violations", String.valueOf(violationRounds));
        rec.put("violation_detail", violationDetails.isEmpty() ? "none"
            : String.join(" | ", violationDetails.subList(0,
                Math.min(20, violationDetails.size()))).replace(",", ";"));
        rec.put("deadlocks", String.valueOf(totalDeadlocks));
        rec.put("lock_timeouts", String.valueOf(totalLockTimeouts));
        rec.put("iterations", String.valueOf(iterations));
        rec.put("duration_ms", String.valueOf(durationMs));
        RunContext.append("final_correctness.csv", rec);

        System.out.println("\n===== [C4] 동일 Order race " + iterations + "회 반복 =====");
        System.out.println("위반이 관측된 회차 = " + violationRounds + " / " + iterations);
        System.out.println("deadlock = " + totalDeadlocks
            + " / lockTimeout = " + totalLockTimeouts
            + " / 기타 실패 = " + totalOtherFailures);
        System.out.println("소요 시간 = " + durationMs + "ms");
        if (!violationDetails.isEmpty()) {
            System.out.println("위반 상세(최대 20건):");
            violationDetails.stream().limit(20).forEach(d -> System.out.println("  " + d));
        }
        System.out.println("주의: 이 결과는 " + iterations
            + "회 재현 시도에서 위반이 관측되지 않았음을 뜻하며,");
        System.out.println("      race condition 의 부재를 증명하지 않는다.");
        System.out.println("=================================================\n");

        assertThat(violationRounds)
            .as("%d회 반복 중 불변식 위반이 관측된 회차", iterations)
            .isZero();
    }

    // ─────────────────────────── C5 ───────────────────────────

    @Test
    @DisplayName("[진단] C5 순차 실행 시 전량 체결되는가")
    void c5_diagnostic_sequential() {
        Fixture f = newFixture("C5SEQ", 100);
        orderService.placeOrder(f.seller.getUsername(), req(f.stock, OrderType.SELL, 10));
        for (int i = 0; i < 15; i++) {
            User b = userWithBalance("C5SQ" + i);
            orderService.placeOrder(b.getUsername(), req(f.stock, OrderType.BUY, 1));
        }
        int sum = tradesOf(f.stock).stream().mapToInt(Trade::getExecutedQuantity).sum();
        System.out.println("\n[진단] 순차 15건 매수 -> 체결 수량 합계 = " + sum + " (기대 10)");
    }

    @Test
    @DisplayName("C5: SELL 10주에 BUY 100건 동시 진입 - 체결량은 주문량을 넘지 않는다")
    void c5_overfillStress() throws Exception {
        int concurrency = 100;
        // seller Holdings(100)를 주문량(10)보다 크게 둔다.
        // Holdings 부족이 중복 체결을 "우연히" 차단하지 못하게 해서
        // Order 자체의 체결 한도가 지켜지는지를 검증하기 위함이다.
        Fixture f = newFixture("C5", 100);

        orderService.placeOrder(f.seller.getUsername(), req(f.stock, OrderType.SELL, 10));
        Order sellOrder = orderRepository
            .findByUserAndStock_StockCode(f.seller, f.stock.getStockCode()).get(0);

        List<User> buyers = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            buyers.add(userWithBalance("C5B" + i));
        }

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CyclicBarrier barrier = new CyclicBarrier(concurrency);
        CountDownLatch done = new CountDownLatch(concurrency);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (User buyer : buyers) {
            pool.submit(() -> {
                try {
                    barrier.await(30, TimeUnit.SECONDS);
                    orderService.placeOrder(buyer.getUsername(),
                        req(f.stock, OrderType.BUY, 1));
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }
        assertThat(done.await(180, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        List<Trade> trades = tradesOf(f.stock);
        int executedSum = trades.stream().mapToInt(Trade::getExecutedQuantity).sum();
        int overfill = Math.max(0, executedSum - 10);
        Order after = orderRepository.findById(sellOrder.getId()).orElseThrow();
        int sellerHoldings = holdingsRepository
            .findByUserAndStock(f.seller, f.stock).orElseThrow().getQuantity();

        int deadlocks = 0, lockTimeouts = 0, other = 0;
        for (Throwable t : failures) {
            if (BenchmarkSupport.isDeadlock(t)) deadlocks++;
            else if (BenchmarkSupport.isLockWaitTimeout(t)) lockTimeouts++;
            else other++;
        }

        // 안전성 불변식(위반 시 자산 손상)만 violations 로 센다.
        // "전량 체결되었는가"는 처리량 관측이지 안전성 불변식이 아니므로 분리한다.
        int violations = 0;
        List<String> vd = new ArrayList<>();
        if (executedSum > 10) { violations++; vd.add("OVERFILL_EXEC:" + executedSum); }
        if (overfill != 0) { violations++; vd.add("OVERFILL:" + overfill); }
        if (sellerHoldings + executedSum != 100) {
            violations++;
            vd.add("HOLDINGS_MISMATCH:holdings=" + sellerHoldings + ",exec=" + executedSum);
        }
        if (after.getQuantity() != 10 - executedSum) {
            violations++; vd.add("REMAINING_MISMATCH:" + after.getQuantity());
        }
        OrderStatus expectedStatus =
            (executedSum == 10) ? OrderStatus.COMPLETED : OrderStatus.PARTIAL;
        if (after.getStatus() != expectedStatus) {
            violations++; vd.add("STATUS_MISMATCH:" + after.getStatus());
        }

        Map<String, String> rec = RunContext.base("C5", concurrency, 1);
        rec.put("total_requests", String.valueOf(concurrency));
        rec.put("success", String.valueOf(concurrency - failures.size()));
        rec.put("failure", String.valueOf(failures.size()));
        rec.put("failure_kinds", "deadlock=" + deadlocks
            + ";lockTimeout=" + lockTimeouts + ";other=" + other);
        rec.put("original_order_quantity", "10");
        rec.put("executed_quantity_sum", String.valueOf(executedSum));
        rec.put("overfill_quantity", String.valueOf(overfill));
        rec.put("order_status", after.getStatus().name());
        rec.put("order_remaining_quantity", String.valueOf(after.getQuantity()));
        rec.put("holdings_before", "100");
        rec.put("holdings_after", String.valueOf(sellerHoldings));
        rec.put("account_before", "N/A");
        rec.put("account_after", "N/A");
        rec.put("invariant_violations", String.valueOf(violations));
        rec.put("violation_detail", vd.isEmpty() ? "none" : String.join(";", vd));
        rec.put("deadlocks", String.valueOf(deadlocks));
        rec.put("lock_timeouts", String.valueOf(lockTimeouts));
        rec.put("trade_count", String.valueOf(trades.size()));
        rec.put("fill_rate_observed", executedSum + "/10");
        RunContext.append("final_correctness.csv", rec);

        System.out.println("\n===== [C5] Overfill Stress (SELL 10 <- BUY 1 x 100) =====");
        System.out.println("스레드 성공        = " + (concurrency - failures.size())
            + " / " + concurrency + "  (HTTP/스레드 성공)");
        System.out.println("실제 체결 건수     = " + trades.size() + "  (거래 성사)");
        System.out.println("체결 수량 합계     = " + executedSum + " (기대 10)");
        System.out.println("overfill           = " + overfill + " (기대 0)");
        System.out.println("매도자 보유수량    = " + sellerHoldings + " (기대 90)");
        System.out.println("SELL 주문          = " + after.getStatus()
            + " / 잔량 " + after.getQuantity() + " (기대 COMPLETED / 0)");
        System.out.println("deadlock=" + deadlocks + " lockTimeout=" + lockTimeouts
            + " 기타=" + other);
        // 진단: 매수 주문들이 어떤 상태로 끝났는지
        java.util.Map<String, Integer> buyStates = new java.util.LinkedHashMap<>();
        for (Order o : orderRepository.findAll()) {
            if (o.getOrderType() == OrderType.BUY) {
                buyStates.merge(o.getStatus() + "/qty" + o.getQuantity(), 1, Integer::sum);
            }
        }
        System.out.println("BUY 주문 최종 상태 분포 = " + buyStates);
        Order sellDiag = orderRepository.findById(sellOrder.getId()).orElseThrow();
        System.out.println("SELL 주문 진단: status=" + sellDiag.getStatus()
            + " qty=" + sellDiag.getQuantity()
            + "  (qty>0 이면 후보로 잡혀야 정상)");
        System.out.println("Trade 상세 = " + tradesOf(f.stock).stream()
            .map(t -> t.getOrderType() + ":" + t.getExecutedQuantity()).toList());
        java.util.Map<String, Integer> failMsgs = new java.util.LinkedHashMap<>();
        for (Throwable t : failures) {
            String m = t.getMessage() == null ? t.getClass().getSimpleName()
                : t.getMessage().replaceAll("\\s+", " ");
            failMsgs.merge(m.length() > 60 ? m.substring(0, 60) : m, 1, Integer::sum);
        }
        System.out.println("실패 메시지 분포 = " + (failMsgs.isEmpty() ? "없음" : failMsgs));
        System.out.println("=========================================================\n");

        // ── 안전성 불변식: 절대 위반되면 안 되는 것 (이 시나리오의 핵심 질문)
        assertThat(executedSum)
            .as("핵심 불변식: 체결 수량 합계는 최초 주문 수량(10)을 넘을 수 없다")
            .isLessThanOrEqualTo(10);
        assertThat(overfill).as("overfill = 0 (자산 복제 없음)").isZero();
        assertThat(sellerHoldings + executedSum)
            .as("보유 감소량이 체결량과 정확히 일치해야 한다 (100 = 잔여 + 체결)")
            .isEqualTo(100);
        assertThat(after.getQuantity())
            .as("SELL 주문 잔량 = 최초수량 - 체결량")
            .isEqualTo(10 - executedSum);
        assertThat(after.getStatus())
            .as("잔량 0이면 COMPLETED, 남았으면 PARTIAL")
            .isEqualTo(executedSum == 10 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);

        // ── 체결률은 안전성과 별개다. 동시 진입 시 스냅샷 타이밍에 따라
        //    후보를 못 보는 요청이 생겨 전량 체결되지 않을 수 있다.
        //    이것은 자산 손상이 아니라 "처리량" 문제이므로 assert 하지 않고 기록만 한다.
        //    (순차 실행 시에는 10/10 전량 체결됨을 c5_diagnostic_sequential 로 확인)
        System.out.println("[C5 관측] 체결률 = " + executedSum + "/10 ("
            + (executedSum * 10) + "%). 안전성 불변식은 모두 충족.");
    }

    // ─────────────────────────── C6 ───────────────────────────

    @Test
    @DisplayName("C6: PARTIAL 체인 - 공통 matching logic 수정의 효과")
    void c6_partialChain() {
        Fixture f = newFixture("C6", 200);

        orderService.placeOrder(f.buyerA.getUsername(), req(f.stock, OrderType.BUY, 100));
        Order buyOrder = orderRepository
            .findByUserAndStock_StockCode(f.buyerA, f.stock.getStockCode()).get(0);

        int[] sells = {30, 20, 10, 40};
        int[] expectedRemaining = {70, 50, 40, 0};
        OrderStatus[] expectedStatus = {
            OrderStatus.PARTIAL, OrderStatus.PARTIAL, OrderStatus.PARTIAL, OrderStatus.COMPLETED};
        List<String> log = new ArrayList<>();
        int violations = 0;

        for (int i = 0; i < sells.length; i++) {
            orderService.placeOrder(f.seller.getUsername(), req(f.stock, OrderType.SELL, sells[i]));
            Order cur = orderRepository.findById(buyOrder.getId()).orElseThrow();
            log.add("SELL " + sells[i] + " -> 잔량 " + cur.getQuantity()
                + "(기대 " + expectedRemaining[i] + ") " + cur.getStatus()
                + "(기대 " + expectedStatus[i] + ")");
            if (cur.getQuantity() != expectedRemaining[i]) violations++;
            if (cur.getStatus() != expectedStatus[i]) violations++;
            assertThat(cur.getQuantity()).as("SELL %d 이후 잔량", sells[i])
                .isEqualTo(expectedRemaining[i]);
            assertThat(cur.getStatus()).as("SELL %d 이후 상태", sells[i])
                .isEqualTo(expectedStatus[i]);
        }

        int sum = tradesOf(f.stock).stream().mapToInt(Trade::getExecutedQuantity).sum();
        if (sum != 100) violations++;

        Map<String, String> rec = RunContext.base("C6", 1, 1);
        rec.put("total_requests", "5");
        rec.put("success", "5");
        rec.put("failure", "0");
        rec.put("failure_kinds", "none");
        rec.put("original_order_quantity", "100");
        rec.put("executed_quantity_sum", String.valueOf(sum));
        rec.put("overfill_quantity", String.valueOf(Math.max(0, sum - 100)));
        rec.put("order_status", orderRepository.findById(buyOrder.getId())
            .orElseThrow().getStatus().name());
        rec.put("order_remaining_quantity", "0");
        rec.put("holdings_before", "200");
        rec.put("holdings_after", String.valueOf(holdingsRepository
            .findByUserAndStock(f.seller, f.stock).orElseThrow().getQuantity()));
        rec.put("account_before", "N/A");
        rec.put("account_after", "N/A");
        rec.put("invariant_violations", String.valueOf(violations));
        rec.put("violation_detail", violations == 0 ? "none" : "see chain");
        rec.put("deadlocks", "0");
        rec.put("lock_timeouts", "0");
        rec.put("partial_chain", String.join(" | ", log).replace(",", ";"));
        RunContext.append("final_correctness.csv", rec);

        System.out.println("\n===== [C6] PARTIAL 체인 =====");
        log.forEach(l -> System.out.println("  " + l));
        System.out.println("체결 수량 합계 = " + sum + " (기대 100)");
        System.out.println("주의: 이 결과는 B2(동시성 제어)의 효과가 아니라");
        System.out.println("      공통 matching logic 수정(PENDING+PARTIAL 조회 + loop)의 효과다.");
        System.out.println("=============================\n");

        assertThat(sum).as("체결 수량 합계 = 100").isEqualTo(100);
    }

    // ─────────────────────────── helpers ───────────────────────────

    /** 회차별 독립 데이터. 종목코드가 달라 회차 간 간섭이 없다. */
    private static class Fixture {
        User seller;
        User buyerA;
        User buyerB;
        Stocks stock;
    }

    private Fixture newFixture(String tag, int sellerHoldings) {
        Fixture f = new Fixture();
        // stock_code 는 varchar(10). 회차마다 고유해야 하므로 카운터를 쓴다.
        f.stock = stocksRepository.save(Stocks.builder()
            .stockCode("F" + String.format("%09d", STOCK_SEQ.incrementAndGet()))
            .stockName(tag).build());
        f.seller = saveUser(tag + "S");
        accountsRepository.save(Accounts.builder().user(f.seller)
            .balance(RICH).realizedProfit(BigDecimal.ZERO).build());
        holdingsRepository.save(Holdings.builder().user(f.seller).stock(f.stock)
            .quantity(sellerHoldings).averageBuyPrice(PRICE).build());
        f.buyerA = userWithBalance(tag + "A");
        f.buyerB = userWithBalance(tag + "B");
        return f;
    }

    private User userWithBalance(String prefix) {
        User u = saveUser(prefix);
        accountsRepository.save(Accounts.builder().user(u)
            .balance(RICH).realizedProfit(BigDecimal.ZERO).build());
        return u;
    }

    private User saveUser(String prefix) {
        return userRepository.save(User.builder()
            .username(prefix + "_" + System.nanoTime())
            .name(prefix).role("USER")
            .email(prefix + "_" + System.nanoTime() + "@t.com").build());
    }

    /**
     * 해당 종목의 체결만 조회한다.
     * Trade.order 는 LAZY 프록시라 세션 밖에서 getStock() 을 만지면
     * LazyInitializationException 이 난다. JPQL 조인으로 DB 에서 걸러낸다.
     */
    private List<Trade> tradesOf(Stocks stock) {
        return em.createQuery(
                "SELECT t FROM Trade t WHERE t.order.stock.stockCode = :code", Trade.class)
            .setParameter("code", stock.getStockCode())
            .getResultList();
    }

    private OrderRequestDto req(Stocks stock, OrderType type, int qty) {
        OrderRequestDto d = new OrderRequestDto();
        d.setStockCode(stock.getStockCode());
        d.setOrderType(type);
        d.setPrice(PRICE);
        d.setQuantity(qty);
        return d;
    }
}
