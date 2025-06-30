package com.example.iotl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.UserRepository;
import java.math.BigDecimal;
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
    HoldingsRepository holdingsRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StockInfoRepository stockInfoRepository;

    User user;
    Stocks stock;

    @BeforeEach
    void setUp(){
        user = userRepository.save(User.builder().name("concurrentUser").build());
        stock = stockInfoRepository.save(Stocks.builder().stockCode("A001").build());
        holdingsRepository.save(Holdings.builder()
            .user(user)
            .stock(stock)
            .quantity(100)
            .averageBuyPrice(new BigDecimal("1000"))
            .build());
    }

    @AfterEach
    void tearDown(){
        holdingsRepository.deleteAll();
        userRepository.deleteAll();
        stockInfoRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 여러 번 매도 시 보유수량이 음수로 내려가지 않는다. (락/트랜잭션 적용)")
    void concurrentSellTest() throws InterruptedException{

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Order order = orderRepository.save(Order.builder()
            .user(user)
            .stock(stock)
            .orderType(OrderType.SELL)
            .price(new BigDecimal("1000"))
            .quantity(1)
            .status(OrderStatus.PENDING)
            .build());

        for (int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    tradeService.trade(order,order);
                }catch (Exception e){

                }finally {
                    latch.countDown();
                }

            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Holdings holdings = holdingsRepository.findByUserAndStock(user, stock).orElseThrow();
        System.out.println("최종 보유 수량 = " + holdings.getQuantity());
        assertThat(holdings.getQuantity()).isGreaterThanOrEqualTo(0);

    }





  
}