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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
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
 * [2단계] 동일 PENDING 주문이 두 거래에서 중복 체결되는 문제 재현.
 *
 * <p>근거가 되는 production 코드:
 * <ul>
 *   <li>OrderRepository.findMatchingOrders():17-25 — @Lock 이 없다. 두 트랜잭션이 같은
 *       PENDING sellOrder 행을 각각 읽어 갈 수 있다.</li>
 *   <li>TradeService.trade():29-119 — 인자로 받은 matchedOrder 의 status 를 재검증하지 않는다.
 *       이미 COMPLETED 된 주문이 넘어와도 그대로 체결한다.</li>
 *   <li>TradeService.trade():62 — tradeQuantity 는 sellerHoldings 잔량으로만 제한된다.
 *       Holdings 비관적 락은 두 트랜잭션을 "직렬화"할 뿐, 두 번째 체결을 "거부"하지 않는다.</li>
 * </ul>
 *
 * <p>기존 TradeServiceConcurrencyTest 와 결정적으로 다른 점:
 * <ol>
 *   <li>tradeService.trade() 를 직접 호출하지 않고 OrderService.placeOrder() 를 진입점으로 쓴다.
 *       그래야 매칭 후보 조회(findMatchingOrders)가 경쟁에 포함된다.</li>
 *   <li>CyclicBarrier 로 두 스레드가 placeOrder 진입 시점을 맞춘다.</li>
 *   <li>{@code >= 0} 같은 느슨한 검증을 쓰지 않고 정확한 기대값을 assert 한다.</li>
 * </ol>
 */
@DisplayName("[2단계] 동일 sellOrder 중복 체결 재현")
class DuplicateFillConcurrencyTest extends TradingSliceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired TradeRepository tradeRepository;
    @Autowired HoldingsRepository holdingsRepository;
    @Autowired AccountsRepository accountsRepository;
    @Autowired UserRepository userRepository;
    @Autowired StocksRepository stocksRepository;

    private static final BigDecimal PRICE = new BigDecimal("1000");
    private static final int SELLER_INITIAL_HOLDINGS = 10;
    private static final BigDecimal BUYER_INITIAL_BALANCE = new BigDecimal("1000000.00");

    private User seller;
    private User buyerA;
    private User buyerB;
    private Stocks stock;

    @BeforeEach
    void setUp() {
        cleanUp();

        seller = saveUser("SELLER");
        buyerA = saveUser("BUYER_A");
        buyerB = saveUser("BUYER_B");

        accountsRepository.save(Accounts.builder()
            .user(seller).balance(new BigDecimal("0.00")).realizedProfit(BigDecimal.ZERO).build());
        accountsRepository.save(Accounts.builder()
            .user(buyerA).balance(BUYER_INITIAL_BALANCE).realizedProfit(BigDecimal.ZERO).build());
        accountsRepository.save(Accounts.builder()
            .user(buyerB).balance(BUYER_INITIAL_BALANCE).realizedProfit(BigDecimal.ZERO).build());

        stock = stocksRepository.save(Stocks.builder()
            .stockCode("C" + (System.nanoTime() % 100000)).stockName("중복체결테스트").build());

        // 매도자는 10주를 보유하지만, 매도 "주문"은 1주만 낼 것이다.
        // 보유량을 넉넉히 준 이유: 중복 체결이 Holdings 부족으로 막히는 게 아니라
        // 주문 수량 통제 부재로 발생한다는 것을 분리해서 보여주기 위함.
        holdingsRepository.save(Holdings.builder()
            .user(seller).stock(stock)
            .quantity(SELLER_INITIAL_HOLDINGS).averageBuyPrice(PRICE).build());
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private User saveUser(String prefix) {
        return userRepository.save(User.builder()
            .username(prefix + "_" + System.nanoTime())
            .name(prefix).role("USER")
            .email(prefix + "_" + System.nanoTime() + "@test.com")
            .build());
    }

    private void cleanUp() {
        tradeRepository.deleteAll();
        orderRepository.deleteAll();
        holdingsRepository.deleteAll();
        accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1주짜리 SELL 주문에 BUY 2건이 동시 진입해도 체결은 정확히 1회여야 한다")
    void sameSellOrderMustNotBeFilledTwice() throws Exception {
        // ── given: 1주짜리 SELL 주문 1건만 PENDING 으로 대기
        orderService.placeOrder(seller.getUsername(), orderRequest(OrderType.SELL, 1));

        Order sellOrder = orderRepository.findByUserAndStock_StockCode(
            seller, stock.getStockCode()).get(0);
        assertThat(sellOrder.getStatus())
            .as("사전 조건: 매칭 상대가 없으므로 SELL 주문은 PENDING 이어야 한다")
            .isEqualTo(OrderStatus.PENDING);
        assertThat(sellOrder.getQuantity()).isEqualTo(1);

        // ── when: 서로 다른 매수자 2명이 각각 1주 매수 주문을 동시에 낸다
        int threadCount = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        // CyclicBarrier: 두 스레드가 placeOrder 호출 직전까지 서로를 기다린 뒤 동시에 출발.
        // 이렇게 해야 두 트랜잭션이 같은 PENDING sellOrder 를 읽을 확률이 최대가 된다.
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        List<Throwable> failures = new ArrayList<>();

        for (User buyer : List.of(buyerA, buyerB)) {
            pool.submit(() -> {
                try {
                    startBarrier.await(5, TimeUnit.SECONDS);
                    orderService.placeOrder(buyer.getUsername(), orderRequest(OrderType.BUY, 1));
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    // 예외를 삼키지 않고 수집한다. 실패 원인을 알아야 재현 여부를 판단할 수 있다.
                    synchronized (failures) {
                        failures.add(t);
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertThat(doneLatch.await(30, TimeUnit.SECONDS))
            .as("두 스레드가 30초 안에 끝나야 한다 (데드락/락 대기 여부 확인)")
            .isTrue();
        pool.shutdown();

        // ── 재현 결과 수집
        Order sellOrderAfter = orderRepository.findById(sellOrder.getId()).orElseThrow();
        List<Trade> tradesOnSellOrder = allTrades();
        Holdings sellerHoldings =
            holdingsRepository.findByUserAndStock(seller, stock).orElseThrow();

        printReport(sellOrderAfter, tradesOnSellOrder, sellerHoldings,
            successCount.get(), failures);

        // ── then: 1주짜리 매도 주문이므로 체결은 정확히 1회여야 한다
        assertThat(tradesOnSellOrder)
            .as("SELL 주문은 1주뿐이므로 전체 체결 기록은 정확히 1건이어야 한다")
            .hasSize(1);

        assertThat(sellerHoldings.getQuantity())
            .as("1주만 팔렸으므로 매도자 보유수량은 %d - 1 = %d 여야 한다",
                SELLER_INITIAL_HOLDINGS, SELLER_INITIAL_HOLDINGS - 1)
            .isEqualTo(SELLER_INITIAL_HOLDINGS - 1);

        assertThat(sellOrderAfter.getQuantity())
            .as("SELL 주문 잔량은 0 이어야 한다")
            .isEqualTo(0);
        assertThat(sellOrderAfter.getStatus())
            .as("SELL 주문은 COMPLETED 여야 한다")
            .isEqualTo(OrderStatus.COMPLETED);

        // 매수 주문 2건 중 정확히 1건만 체결되고, 나머지 1건은 PENDING 으로 남아야 한다.
        List<Order> buyOrders = new ArrayList<>();
        buyOrders.addAll(orderRepository.findByUserAndStock_StockCode(buyerA, stock.getStockCode()));
        buyOrders.addAll(orderRepository.findByUserAndStock_StockCode(buyerB, stock.getStockCode()));

        long completedBuys = buyOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();

        // [핵심 안전성] 매도 물량이 1주뿐이므로 체결된 BUY 주문은 정확히 1건.
        assertThat(completedBuys)
            .as("매도 물량이 1주뿐이므로 체결된 BUY 주문은 정확히 1건이어야 한다")
            .isEqualTo(1L);

        // 패자 주문의 "운명"은 후보마다 다르므로 안전성 검증에서 분리한다.
        //  - 트랜잭션이 통째로 롤백되면 주문 자체가 남지 않는다(주문 1건).
        //  - 재조회로 회복하면 PENDING 으로 남는다(주문 2건).
        // 어느 쪽이든 "중복 체결이 없다"는 안전성은 동일하게 성립하므로,
        // 여기서는 기록만 하고 A/B 비교 항목으로 넘긴다.
        System.out.println("[패자 주문 처리] 저장된 BUY 주문 수 = " + buyOrders.size()
            + " (2=PENDING 으로 잔존, 1=롤백으로 소멸)");

        // 계좌: 매수자 중 정확히 1명만 1000원이 빠져나가야 한다.
        BigDecimal buyerATotal = balanceOf(buyerA);
        BigDecimal buyerBTotal = balanceOf(buyerB);
        BigDecimal spentTotal = BUYER_INITIAL_BALANCE.subtract(buyerATotal)
            .add(BUYER_INITIAL_BALANCE.subtract(buyerBTotal));
        assertThat(spentTotal)
            .as("두 매수자의 출금 합계는 1주 * 1000원 = 1000원이어야 한다")
            .isEqualByComparingTo(PRICE);

        assertThat(balanceOf(seller))
            .as("매도자 잔액은 1주 매도 대금 1000원이어야 한다")
            .isEqualByComparingTo(PRICE);
    }

    private void printReport(Order sellOrderAfter, List<Trade> trades,
        Holdings sellerHoldings, int successCount, List<Throwable> failures) {

        System.out.println("\n============ [2단계] 동시 체결 재현 결과 ============");
        System.out.println("placeOrder 성공 스레드 수 = " + successCount + " / 2");
        System.out.println("placeOrder 예외 스레드 수 = " + failures.size());
        for (Throwable t : failures) {
            System.out.println("   └ 예외: " + t.getClass().getSimpleName() + " : " + t.getMessage());
        }
        System.out.println("--------------------------------------------------");
        System.out.printf("%-28s %-10s %-10s%n", "검증 항목", "기대값", "실제값");
        row("체결(Trade) 건수", 1, trades.size());
        row("매도자 보유수량", SELLER_INITIAL_HOLDINGS - 1, sellerHoldings.getQuantity());
        row("SELL 주문 잔량", 0, sellOrderAfter.getQuantity());
        System.out.printf("%-28s %-10s %-10s%n", "SELL 주문 상태",
            OrderStatus.COMPLETED, sellOrderAfter.getStatus());

        int totalExecuted = trades.stream().mapToInt(Trade::getExecutedQuantity).sum();
        row("체결 수량 합계", 1, totalExecuted);

        for (User buyer : List.of(buyerA, buyerB)) {
            List<Order> os = orderRepository.findByUserAndStock_StockCode(
                buyer, stock.getStockCode());
            for (Order o : os) {
                System.out.printf("%-28s %-10s %-10s%n",
                    "BUY 주문 상태(" + buyer.getName() + ")", "COMPLETED 또는 PENDING", o.getStatus());
            }
            System.out.printf("%-28s %-10s %-10s%n",
                "잔액(" + buyer.getName() + ")", "999000 또는 1000000", balanceOf(buyer));
        }
        System.out.printf("%-28s %-10s %-10s%n", "잔액(seller)", "1000.00", balanceOf(seller));
        System.out.println("==================================================\n");
    }

    private void row(String label, int expected, int actual) {
        System.out.printf("%-28s %-10d %-10d %s%n", label, expected, actual,
            expected == actual ? "" : "  <-- 불일치");
    }

    private BigDecimal balanceOf(User user) {
        return accountsRepository.findAll().stream()
            .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
            .findFirst().orElseThrow().getBalance();
    }

    private List<Trade> allTrades() {
        return tradeRepository.findAll();
    }

    private OrderRequestDto orderRequest(OrderType type, int quantity) {
        OrderRequestDto dto = new OrderRequestDto();
        dto.setStockCode(stock.getStockCode());
        dto.setOrderType(type);
        dto.setPrice(PRICE);
        dto.setQuantity(quantity);
        return dto;
    }
}
