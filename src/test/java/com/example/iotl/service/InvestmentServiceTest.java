package com.example.iotl.service;

import com.example.iotl.dto.profit.ProfitResponse;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.Trade;
import com.example.iotl.entity.User;
import com.example.iotl.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.iotl.entity.Order.OrderType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InvestmentServiceTest {

    private TradeRepository tradeRepository;
    private InvestmentService investmentService;

    @BeforeEach
    void setUp() {
        tradeRepository = mock(TradeRepository.class);
        investmentService = new InvestmentService(tradeRepository);
    }

    @Test
    void testRealizedProfitGraph_ReturnsCorrectTotalAndPoints() {
        // given
        User mockUser = User.builder().username("tester").build();
        Stocks samsung = Stocks.builder().stockName("Samsung").stockCode("005930").build();

        Order order1 = Order.builder()
                .user(mockUser)
                .stock(samsung)
                .orderType(OrderType.SELL)
                .build();

        Trade trade1 = Trade.builder()
                .order(order1)
                .executedPrice(BigDecimal.valueOf(10000))
                .executedQuantity(2)
                .orderType(OrderType.SELL)
                .executedAt(LocalDateTime.now().minusDays(2))
                .build();

        Trade trade2 = Trade.builder()
                .order(order1)
                .executedPrice(BigDecimal.valueOf(20000))
                .executedQuantity(1)
                .orderType(OrderType.SELL)
                .executedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(tradeRepository.findSellTradesByUserAndPeriod(
                ArgumentMatchers.eq(mockUser),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(trade1, trade2));

        // when
        ProfitResponse result = investmentService.getRealizedProfitGraph(mockUser, "1week");

        // then
        assertThat(result.getTotalProfit()).isEqualTo(10000 * 2 + 20000 * 1); // 40000
        assertThat(result.getPoints()).hasSize(2); // 두 날짜
        assertThat(result.getPoints().get(0).getAmount()).isLessThan(result.getPoints().get(1).getAmount());
    }
}