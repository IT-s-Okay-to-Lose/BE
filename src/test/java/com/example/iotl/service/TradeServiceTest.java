package com.example.iotl.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.testUtil.TestEntityFactory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("실현 수익 누적 테스트")
class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private HoldingsRepository holdingsRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    StocksRepository stocksRepository;

    @Autowired
    private EntityManager em; // flush 용도

    @Test
    void 매도자는_실현수익이_정상적으로_누적된다() {
        // given
        User buyer = userRepository.save(TestEntityFactory.createUser("buyer")); // 🔥 save 추가
        User seller = userRepository.save(TestEntityFactory.createUser("seller")); // 🔥 save 추가
        Stocks stock = stocksRepository.save(TestEntityFactory.createStock("삼성전자")); // 🔥 save 추가

        Accounts buyerAccount = accountsRepository.save(
            Accounts.builder().user(buyer).balance(new BigDecimal("1000000")).realizedProfit(BigDecimal.ZERO).build()
        );
        Accounts sellerAccount = accountsRepository.save(
            Accounts.builder().user(seller).balance(new BigDecimal("0")).realizedProfit(BigDecimal.ZERO).build()
        );

        holdingsRepository.save(
            Holdings.builder().user(seller).stock(stock).quantity(10).averageBuyPrice(new BigDecimal("1000")).build()
        );

        Order buyOrder = orderRepository.save(
            Order.builder().user(buyer).status(OrderStatus.PENDING).stock(stock).orderType(OrderType.BUY).price(new BigDecimal("50000")).quantity(5).build()
        );

        Order sellOrder = orderRepository.save(
            Order.builder().user(seller).stock(stock).status(OrderStatus.PENDING).orderType(OrderType.SELL).price(new BigDecimal("50000")).quantity(5).build()
        );

        em.flush();
        em.clear();

        // when
        tradeService.trade(buyOrder, sellOrder);

        // then
        Accounts updatedSellerAccount = accountsRepository.findByUserWithPessimisticLock(seller)
            .orElseThrow();

        BigDecimal expectedProfit = new BigDecimal("50000").multiply(BigDecimal.valueOf(5)).setScale(2);

        assertEquals(expectedProfit, updatedSellerAccount.getRealizedProfit());
        System.out.println("📈 판매자 실현 수익: " + updatedSellerAccount.getRealizedProfit());

    }

}
