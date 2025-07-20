package com.example.iotl.service;

import com.example.iotl.dto.profit.ProfitResponse;
import com.example.iotl.entity.Trade;
import com.example.iotl.entity.User;
import com.example.iotl.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.iotl.entity.Order.OrderType;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final TradeRepository tradeRepository;

    public ProfitResponse getRealizedProfitGraph(User user, String period) {
        LocalDateTime start = getStartDateFromPeriod(period);
        LocalDateTime end = LocalDateTime.now();

        List<Trade> sellTrades = tradeRepository.findSellTradesByUserAndPeriod(user, start, end);

        // 날짜별 누적 수익 계산
        Map<LocalDate, Long> dailyProfitMap = new TreeMap<>();
        long runningTotal = 0;

        for (Trade trade : sellTrades) {
            LocalDate date = trade.getExecutedAt().toLocalDate();
            int qty = trade.getExecutedQuantity();
            BigDecimal price = trade.getExecutedPrice();

            // 간단히 매입가는 생략하고 매출액으로 처리
            long profit = price.multiply(BigDecimal.valueOf(qty)).longValue();
            runningTotal += profit;

            dailyProfitMap.put(date, runningTotal); // 누적 저장
        }

        List<ProfitResponse.ProfitPoint> points = dailyProfitMap.entrySet().stream()
                .map(e -> new ProfitResponse.ProfitPoint(
                        e.getKey().format(DateTimeFormatter.ofPattern("MM.dd")),
                        e.getValue()
                ))
                .toList();

        return new ProfitResponse(runningTotal, points);
    }

    private LocalDateTime getStartDateFromPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "1day" -> now.minusDays(1);
            case "1week" -> now.minusWeeks(1);
            case "1month" -> now.minusMonths(1);
            case "3month" -> now.minusMonths(3);
            default -> throw new IllegalArgumentException("유효하지 않은 기간: " + period);
        };
    }
}