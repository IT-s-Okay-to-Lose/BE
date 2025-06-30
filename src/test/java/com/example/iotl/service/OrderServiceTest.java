package com.example.iotl.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.iotl.dto.order.OrderRequestDto;
import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StockInfoRepository stockInfoRepository;
    @Autowired
    HoldingsRepository holdingsRepository;
    @Autowired
    AccountsRepository accountsRepository;

    User seller;
    Stocks stock;

    @BeforeEach
    void setUp() {
        // SELLER 생성
        seller = userRepository.save(User.builder()
            .username("SELLER" + System.nanoTime())
            .name("seller")
            .role("USER")
            .email("seller" + System.currentTimeMillis() + "@test.com")
            .build());
        Accounts sellerAcc = Accounts.builder()
            .user(seller)
            .balance(new BigDecimal("10000"))
            .build();
        accountsRepository.save(sellerAcc);
        seller.setAccount(sellerAcc);
        userRepository.save(seller);

        // 주식 생성
        stock = stockInfoRepository.save(Stocks.builder().stockCode("A001").build());

        // SELLER 보유 주식 1주
        holdingsRepository.save(Holdings.builder()
            .user(seller)
            .stock(stock)
            .quantity(1)
            .averageBuyPrice(new BigDecimal("1000"))
            .build());
    }

    @AfterEach
    void tearDown() {
        holdingsRepository.deleteAll();
        stockInfoRepository.deleteAll();
        accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("보유수량 초과 매도주문 등록 시 예외 발생")
    void cannotPlaceSellOrderWithInsufficientHoldings() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(seller.getUserId());
        requestDto.setStockCode(stock.getStockCode());
        requestDto.setOrderType(OrderType.SELL);
        requestDto.setPrice(new BigDecimal("1000"));
        requestDto.setQuantity(2); // seller는 1주만 보유, 2주 매도주문

        assertThrows(IllegalStateException.class, () ->
            orderService.placeOrder(requestDto)
        );
    }
}
