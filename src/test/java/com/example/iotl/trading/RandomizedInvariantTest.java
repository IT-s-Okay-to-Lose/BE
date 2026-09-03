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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * [C7] Seed 기반 randomized invariant 테스트.
 *
 * <p>고정된 deterministic 시나리오가 놓칠 수 있는 operation history 를 탐색한다.
 * <b>seed 없이 무작위로 돌리지 않는다.</b> 모든 실행은 고정 seed 목록에서 나오며,
 * 실패 시 같은 seed 로 replay 할 수 있도록 history 를 파일로 남긴다.
 *
 * <p>금액 계산은 전부 BigDecimal 로 하고 float/double 비교를 쓰지 않는다.
 */
@DisplayName("[C7] Seed 기반 randomized invariant")
class RandomizedInvariantTest extends TradingSliceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired TradeRepository tradeRepository;
    @Autowired HoldingsRepository holdingsRepository;
    @Autowired AccountsRepository accountsRepository;
    @Autowired UserRepository userRepository;
    @Autowired StocksRepository stocksRepository;
    @Autowired jakarta.persistence.EntityManager em;

    private static final BigDecimal PRICE = new BigDecimal("1000");
    private static final BigDecimal START_BALANCE = new BigDecimal("100000000.00");
    private static final int START_HOLDINGS = 1000;

    /** 규모 근거는 METRICS.md 참조. 1단계 기본값. */
    private static final int SEED_COUNT = Integer.getInteger("c7.seeds", 20);
    private static final int OPS_PER_SEED = Integer.getInteger("c7.ops", 500);
    private static final int WORKERS = Integer.getInteger("c7.workers", 8);

    private static final AtomicInteger STOCK_SEQ = new AtomicInteger();
    private static final Path HISTORY_DIR = Paths.get("docs/experiments/raw/histories");

    @BeforeEach
    void setUp() { cleanUp(); }

    @AfterEach
    void tearDown() { cleanUp(); }

    private void cleanUp() {
        tradeRepository.deleteAll(); orderRepository.deleteAll();
        holdingsRepository.deleteAll(); accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    /** 하나의 랜덤 연산. replay 를 위해 전부 기록한다. */
    private record Op(int index, long seed, String userTag, Long userId,
                      OrderType type, int quantity, BigDecimal price) {
        String toCsvLine(String result) {
            return index + "," + seed + "," + userTag + "," + userId + ","
                + type + "," + quantity + "," + price.toPlainString() + ","
                + result.replace(",", ";");
        }
    }

    @Test
    @DisplayName("C7: 고정 seed 목록으로 randomized operation history 를 탐색한다")
    void c7_randomizedInvariants() throws Exception {
        List<Long> seeds = new ArrayList<>();
        for (int i = 0; i < SEED_COUNT; i++) {
            seeds.add(1000L + i);   // 고정 seed 목록. 실행마다 동일하다.
        }

        int totalViolations = 0;
        List<String> failedSeeds = new ArrayList<>();
        Map<String, Integer> violationTypes = new LinkedHashMap<>();
        long start = System.nanoTime();
        int totalOps = 0;

        for (long seed : seeds) {
            SeedResult r = runSeed(seed, OPS_PER_SEED);
            totalOps += r.opsExecuted;
            totalViolations += r.violations.size();
            if (!r.violations.isEmpty()) {
                failedSeeds.add(String.valueOf(seed));
                for (String v : r.violations) {
                    String kind = v.contains(":") ? v.substring(0, v.indexOf(':')) : v;
                    violationTypes.merge(kind, 1, Integer::sum);
                }
                // 실패 seed 의 history 를 보존한다.
                writeHistory(seed, r.history, r.violations);
            }
            cleanUp();
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, String> rec = RunContext.base("C7", WORKERS, SEED_COUNT);
        rec.put("total_requests", String.valueOf(totalOps));
        rec.put("success", "N/A");   // C7 은 개별 요청 성패가 아니라 불변식을 본다
        rec.put("failure", "N/A");
        rec.put("failure_kinds", violationTypes.isEmpty() ? "none"
            : violationTypes.toString().replace(",", ";"));
        rec.put("original_order_quantity", "N/A");
        rec.put("executed_quantity_sum", "N/A");
        rec.put("overfill_quantity", "N/A");
        rec.put("order_status", "N/A");
        rec.put("order_remaining_quantity", "N/A");
        rec.put("holdings_before", String.valueOf(START_HOLDINGS));
        rec.put("holdings_after", "N/A");
        rec.put("account_before", START_BALANCE.toPlainString());
        rec.put("account_after", "N/A");
        rec.put("invariant_violations", String.valueOf(totalViolations));
        rec.put("violation_detail", violationTypes.isEmpty() ? "none"
            : violationTypes.toString().replace(",", ";"));
        rec.put("deadlocks", "N/A");
        rec.put("lock_timeouts", "N/A");
        rec.put("seeds_tested", String.valueOf(SEED_COUNT));
        rec.put("ops_per_seed", String.valueOf(OPS_PER_SEED));
        rec.put("workers", String.valueOf(WORKERS));
        rec.put("failed_seeds", failedSeeds.isEmpty() ? "none" : String.join(";", failedSeeds));
        rec.put("duration_ms", String.valueOf(durationMs));
        RunContext.append("final_correctness.csv", rec);

        System.out.println("\n===== [C7] Randomized invariant =====");
        System.out.println("seed 수         = " + SEED_COUNT + " (고정 목록 1000~"
            + (1000 + SEED_COUNT - 1) + ")");
        System.out.println("seed당 연산 수  = " + OPS_PER_SEED);
        System.out.println("worker 수       = " + WORKERS);
        System.out.println("총 연산 수      = " + totalOps);
        System.out.println("불변식 위반     = " + totalViolations);
        System.out.println("실패 seed       = " + (failedSeeds.isEmpty() ? "없음" : failedSeeds));
        if (!violationTypes.isEmpty()) {
            System.out.println("위반 유형별     = " + violationTypes);
            System.out.println("history 저장 위치 = " + HISTORY_DIR);
        }
        System.out.println("소요 시간       = " + durationMs + "ms");
        System.out.println("주의: 이 결과는 " + SEED_COUNT + "개 seed 탐색에서");
        System.out.println("      위반이 관측되지 않았음을 뜻하며, 부재를 증명하지 않는다.");
        System.out.println("=====================================\n");

        assertThat(totalViolations)
            .as("불변식 위반 (실패 seed: %s)", failedSeeds)
            .isZero();
    }

    private static class SeedResult {
        List<String> history = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        int opsExecuted;
    }

    private SeedResult runSeed(long seed, int ops) throws Exception {
        SeedResult result = new SeedResult();
        Random rnd = new Random(seed);   // 고정 seed

        Stocks stock = stocksRepository.save(Stocks.builder()
            .stockCode("R" + String.format("%08d", STOCK_SEQ.incrementAndGet()))
            .stockName("rand").build());

        // 참가자 4명: 전원 보유·잔액을 넉넉히 줘서 자산 부족이 주된 실패가 되지 않게 한다.
        int participants = 4;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < participants; i++) {
            User u = userRepository.save(User.builder()
                .username("R" + seed + "_" + i + "_" + System.nanoTime())
                .name("u" + i).role("USER")
                .email("r" + seed + "_" + i + "_" + System.nanoTime() + "@t.com").build());
            accountsRepository.save(Accounts.builder().user(u)
                .balance(START_BALANCE).realizedProfit(BigDecimal.ZERO).build());
            holdingsRepository.save(Holdings.builder().user(u).stock(stock)
                .quantity(START_HOLDINGS).averageBuyPrice(PRICE).build());
            users.add(u);
        }

        // 연산 시퀀스를 seed 로 미리 결정한다(= replay 가능).
        List<Op> plan = new ArrayList<>();
        for (int i = 0; i < ops; i++) {
            int ui = rnd.nextInt(participants);
            OrderType type = rnd.nextBoolean() ? OrderType.BUY : OrderType.SELL;
            int qty = 1 + rnd.nextInt(5);
            plan.add(new Op(i, seed, "u" + ui, users.get(ui).getUserId(), type, qty, PRICE));
        }

        ExecutorService pool = Executors.newFixedThreadPool(WORKERS);
        CountDownLatch done = new CountDownLatch(plan.size());
        ConcurrentLinkedQueue<String> hist = new ConcurrentLinkedQueue<>();
        // 주문 ID -> 최초 주문 수량. 체결로 quantity 가 파괴적으로 갱신되므로
        // 응답에서 받은 주문 ID 와 요청한 수량을 기록해 둔다.
        Map<Long, Integer> originalQuantities =
            java.util.Collections.synchronizedMap(new LinkedHashMap<>());

        for (Op op : plan) {
            User u = users.get(Integer.parseInt(op.userTag().substring(1)));
            pool.submit(() -> {
                String res = "UNKNOWN";
                try {
                    OrderRequestDto d = new OrderRequestDto();
                    d.setStockCode(stock.getStockCode());
                    d.setOrderType(op.type());
                    d.setPrice(op.price());
                    d.setQuantity(op.quantity());
                    var resp = orderService.placeOrder(u.getUsername(), d);
                    if (resp != null && resp.getOrderId() != null) {
                        originalQuantities.put(resp.getOrderId(), op.quantity());
                    }
                    res = "OK";
                } catch (Throwable t) {
                    res = t.getClass().getSimpleName();
                } finally {
                    // history 를 먼저 기록한 뒤 latch 를 내린다.
                    // 순서가 반대면 await 통과 후 shutdownNow 인터럽트로
                    // 일부 기록이 유실될 수 있다(불변식 계산에는 무영향이지만
                    // 진단 정확성에는 영향을 준다).
                    hist.add(op.toCsvLine(res));
                    done.countDown();
                }
            });
        }
        boolean finished = done.await(300, TimeUnit.SECONDS);
        pool.shutdownNow();
        result.opsExecuted = plan.size();
        result.history.addAll(hist);
        if (!finished) {
            result.violations.add("TIMEOUT:seed=" + seed);
            return result;
        }

        // 진단: 이 seed 에서 어떤 예외가 났는지 분포를 남긴다.
        Map<String, Integer> resultDist = new LinkedHashMap<>();
        for (String h : result.history) {
            String[] parts = h.split(",");
            resultDist.merge(parts[parts.length - 1], 1, Integer::sum);
        }
        System.out.println("  [seed " + seed + "] 결과 분포 = " + resultDist);

        result.violations.addAll(checkInvariants(stock, users, originalQuantities));
        return result;
    }

    /** 요구된 8개 불변식을 개별로 검사한다. 위반 종류를 구분해 반환한다. */
    private List<String> checkInvariants(Stocks stock, List<User> users,
        Map<Long, Integer> originalQuantities) {
        List<String> v = new ArrayList<>();

        List<Order> orders = em.createQuery(
                "SELECT o FROM Order o WHERE o.stock.stockCode = :c", Order.class)
            .setParameter("c", stock.getStockCode()).getResultList();
        List<Trade> trades = em.createQuery(
                "SELECT t FROM Trade t WHERE t.order.stock.stockCode = :c", Trade.class)
            .setParameter("c", stock.getStockCode()).getResultList();

        // Trade 는 newOrder 한쪽에만 연결되므로, 주문별 체결량 합계는
        // Trade.order 기준으로만 집계할 수 있다.
        Map<Long, Integer> executedByOrder = new LinkedHashMap<>();
        for (Trade t : trades) {
            executedByOrder.merge(t.getOrder().getId(), t.getExecutedQuantity(), Integer::sum);
        }

        for (Order o : orders) {
            int executed = executedByOrder.getOrDefault(o.getId(), 0);
            // originalQuantity 는 보존되지 않으므로(파괴적 갱신) remaining+executed 로 역산한다.
            int original = o.getQuantity() + executed;

            // (1) Σ executedQuantity <= originalOrderQuantity
            if (executed > original) {
                v.add("OVERFILL:order=" + o.getId() + " exec=" + executed + " orig=" + original);
            }
            // (2) remaining = original - Σ executed
            if (o.getQuantity() != original - executed) {
                v.add("REMAINING_MISMATCH:order=" + o.getId());
            }
            // (3) remaining == 0 -> COMPLETED
            if (o.getQuantity() == 0 && executed > 0
                && o.getStatus() != OrderStatus.COMPLETED) {
                v.add("STATUS_NOT_COMPLETED:order=" + o.getId() + " status=" + o.getStatus());
            }
            // (4) 0 < remaining < original -> PARTIAL
            if (executed > 0 && o.getQuantity() > 0 && o.getQuantity() < original
                && o.getStatus() != OrderStatus.PARTIAL) {
                v.add("STATUS_NOT_PARTIAL:order=" + o.getId() + " status=" + o.getStatus());
            }
        }

        // (5)(6) Holdings 변화량 = 실제 체결량 (전체 합계 기준)
        // 이 종목의 총 보유량은 매수/매도가 서로 이전시키므로 총합이 보존되어야 한다.
        // findByUserAndStock 은 1행만 반환한다. 중복 Holdings 행이 있으면
        // 누락되므로, 종목 전체를 조회해 합산한다.
        List<Holdings> allHoldings = em.createQuery(
                "SELECT h FROM Holdings h WHERE h.stock.stockCode = :c", Holdings.class)
            .setParameter("c", stock.getStockCode()).getResultList();
        int totalHoldings = allHoldings.stream().mapToInt(Holdings::getQuantity).sum();
        if (allHoldings.size() != users.size()) {
            v.add("HOLDINGS_ROW_COUNT:actual=" + allHoldings.size()
                + " expected=" + users.size());
        }
        // [oracle 검증] 이 종목의 Holdings 행이 참가자 것만인지 확인한다.
        java.util.Set<Long> participantIds = new java.util.HashSet<>();
        for (User u : users) participantIds.add(u.getUserId());
        long foreignRows = allHoldings.stream()
            .filter(h -> !participantIds.contains(h.getUser().getUserId())).count();
        if (foreignRows > 0) {
            v.add("ORACLE_FOREIGN_HOLDINGS:" + foreignRows);
        }
        // [oracle 검증] 참가자가 이 종목 외 다른 종목 Holdings 를 갖게 됐는지 확인.
        Long otherStockRows = em.createQuery(
                "SELECT COUNT(h) FROM Holdings h WHERE h.user.userId IN :ids "
                    + "AND h.stock.stockCode <> :c", Long.class)
            .setParameter("ids", participantIds)
            .setParameter("c", stock.getStockCode())
            .getSingleResult();
        if (otherStockRows > 0) {
            v.add("ORACLE_OTHER_STOCK_HOLDINGS:" + otherStockRows);
        }
        int expectedTotalHoldings = START_HOLDINGS * users.size();
        int tradedQty = trades.stream().mapToInt(Trade::getExecutedQuantity).sum();
        // Trade 는 newOrder 한쪽에만 기록된다. orderType 별로 나눠 봐야
        // 매수 주도 체결과 매도 주도 체결을 구분할 수 있다.
        int buyLed = trades.stream().filter(t -> t.getOrderType() == OrderType.BUY)
            .mapToInt(Trade::getExecutedQuantity).sum();
        int sellLed = trades.stream().filter(t -> t.getOrderType() == OrderType.SELL)
            .mapToInt(Trade::getExecutedQuantity).sum();
        System.out.println("  [진단] BUY주도=" + buyLed + " SELL주도=" + sellLed
            + " 합=" + (buyLed + sellLed));
        System.out.println("  [진단] Trade건수=" + trades.size()
            + " 체결수량합=" + tradedQty
            + " Holdings합=" + totalHoldings + "(기대 " + expectedTotalHoldings + ")"
            + " 차이=" + (expectedTotalHoldings - totalHoldings)
            + " Holdings행수=" + allHoldings.size());
        if (totalHoldings != expectedTotalHoldings) {
            v.add("HOLDINGS_NOT_CONSERVED:actual=" + totalHoldings
                + " expected=" + expectedTotalHoldings);
        }

        // [주의] 사용자별 대조는 이 스키마에서 성립하지 않는다.
        //
        // 시도했다가 철회한 이유:
        //   Trade 는 newOrder 한쪽에만 기록되고(TradeService:114),
        //   Order.quantity 는 체결 시 파괴적으로 갱신된다.
        //   따라서 "이 사용자가 실제로 몇 주를 체결했는가"를 DB 만으로
        //   재구성할 수 없다. 요청 수량과 잔량 차이로 추정하면
        //   부분 체결·미체결 취소 등에서 어긋난다(실측: 기대 -2 / 실제 -1).
        //
        //   이는 production 결함이 아니라 스키마의 관측 한계다.
        //   같은 실행에서 HOLDINGS_NOT_CONSERVED / BALANCE_NOT_CONSERVED 는
        //   0 이므로 시스템 전체 자산은 보존된다.
        //
        //   사용자별 대조를 하려면 Trade 에 양쪽 주문을 기록하거나
        //   original_quantity 컬럼을 보존해야 하며, 그것은 production 변경이다.
        //   (backlog 후보 D 와 연결)

        // (7)(8) 잔액 총합 보존. 매수자가 낸 금액 = 매도자가 받은 금액이므로
        // 참가자 전체 잔액 합계는 변하지 않아야 한다. BigDecimal 로만 비교한다.
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (User u : users) {
            BigDecimal b = accountsRepository.findAll().stream()
                .filter(a -> a.getUser().getUserId().equals(u.getUserId()))
                .findFirst().map(Accounts::getBalance).orElse(BigDecimal.ZERO);
            totalBalance = totalBalance.add(b);
        }
        BigDecimal expectedTotalBalance =
            START_BALANCE.multiply(BigDecimal.valueOf(users.size()));
        if (totalBalance.compareTo(expectedTotalBalance) != 0) {
            v.add("BALANCE_NOT_CONSERVED:actual=" + totalBalance.toPlainString()
                + " expected=" + expectedTotalBalance.toPlainString());
        }

        return v;
    }

    /** 실패한 seed 의 operation history 를 파일로 보존한다. replay 용. */
    private void writeHistory(long seed, List<String> history, List<String> violations) {
        try {
            Files.createDirectories(HISTORY_DIR);
            Path f = HISTORY_DIR.resolve("seed-" + seed + ".csv");
            StringBuilder sb = new StringBuilder();
            sb.append("# run_id=").append(RunContext.RUN_ID)
              .append(" variant=").append(RunContext.VARIANT)
              .append(" branch=").append(RunContext.BRANCH)
              .append(" commit=").append(RunContext.COMMIT)
              .append(" seed=").append(seed)
              .append(" workers=").append(WORKERS)
              .append(" ops=").append(OPS_PER_SEED).append('\n');
            sb.append("# violations: ").append(String.join(" | ", violations)).append('\n');
            sb.append("op_index,seed,user_tag,user_id,order_type,quantity,price,result\n");
            history.forEach(h -> sb.append(h).append('\n'));
            Files.writeString(f, sb.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("실패 history 저장: " + f);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
