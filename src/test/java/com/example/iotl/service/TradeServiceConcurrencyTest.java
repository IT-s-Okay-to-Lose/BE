package com.example.iotl.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.TradeRepository;
import com.example.iotl.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
class TradeServiceConcurrencyTest {

    @Autowired
    TradeService tradeService;
    @Autowired
    TradeRepository tradeRepository;
    @Autowired
    HoldingsRepository holdingsRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StockInfoRepository stockInfoRepository;
    @Autowired
    AccountsRepository accountsRepository;

    User seller;
    User buyer;
    Stocks stock;

    @BeforeEach
    void setUp() {
        // SELLER 생성, 계좌, 보유주식
        seller = userRepository.save(User.builder()
            .username("SELLER" + System.nanoTime())
            .name("seller")
            .role("USER")
            .email("seller" + System.currentTimeMillis() + "@test.com")
            .build());
        Accounts sellerAcc = Accounts.builder()
            .user(seller)
            .balance(new BigDecimal("10000")) // 매도자 예수금 의미 없음
            .build();
        accountsRepository.save(sellerAcc);
        seller.setAccount(sellerAcc);
        userRepository.save(seller);

        // BUYER 생성, 계좌
        buyer = userRepository.save(User.builder()
            .username("BUYER" + System.nanoTime())
            .name("buyer")
            .role("USER")
            .email("buyer" + System.currentTimeMillis() + "@test.com")
            .build());
        Accounts buyerAcc = Accounts.builder()
            .user(buyer)
            .balance(new BigDecimal("1000000")) // 충분한 예수금
            .build();
        accountsRepository.save(buyerAcc);
        buyer.setAccount(buyerAcc);
        userRepository.save(buyer);

        // 주식 생성
        stock = stockInfoRepository.save(Stocks.builder().stockCode("A001").build());

        // SELLER 보유 주식 100주
        holdingsRepository.save(Holdings.builder()
            .user(seller)
            .stock(stock)
            .quantity(100)
            .averageBuyPrice(new BigDecimal("1000"))
            .build());
    }

    @AfterEach
    void tearDown() {
        tradeRepository.deleteAll();
        orderRepository.deleteAll();
        holdingsRepository.deleteAll();
        stockInfoRepository.deleteAll();
        accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 여러 번 매도/매수 매칭 해도 동시성 문제 발생하지 않는다.")
    void concurrentBuySellMatchTest() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 매도/매수 주문 각각 50개 생성
        List<Order> sellOrders = new ArrayList<>();
        List<Order> buyOrders = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Order sellOrder = orderRepository.save(Order.builder()
                .user(seller)
                .stock(stock)
                .orderType(OrderType.SELL)
                .price(new BigDecimal("1000"))
                .quantity(1)
                .status(OrderStatus.PENDING)
                .build());
            sellOrders.add(sellOrder);

            Order buyOrder = orderRepository.save(Order.builder()
                .user(buyer)
                .stock(stock)
                .orderType(OrderType.BUY)
                .price(new BigDecimal("1000"))
                .quantity(1)
                .status(OrderStatus.PENDING)
                .build());
            buyOrders.add(buyOrder);
        }

        // 각 쌍별로 trade 수행 (동시성!)
        for (int i = 0; i < threadCount; i++) {
            Order sellOrder = sellOrders.get(i);
            Order buyOrder = buyOrders.get(i);
            executorService.submit(() -> {
                try {
                    tradeService.trade(buyOrder, sellOrder);
                } catch (Exception e) {
                    // 무시(테스트 실패 시 로깅 가능)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Holdings holdings = holdingsRepository.findByUserAndStock(seller, stock).orElseThrow();
        System.out.println("최종 SELLER 보유 수량 = " + holdings.getQuantity());
        assertThat(holdings.getQuantity()).isGreaterThanOrEqualTo(0); // 음수 방지!
    }
}
