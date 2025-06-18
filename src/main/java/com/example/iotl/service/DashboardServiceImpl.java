package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.dto.holding.HoldingRatioDto;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final HoldingsRepository holdingsRepository;
    private final StockDetailRepository stockDetailRepository;

    @Override
    public UserInvestmentSummaryDto getInvestmentSummary(Long userId) {
        // 1. 총 원금 계산
        BigDecimal totalCash = orderRepository.findTotalBuyAmountByUserId(
                userId,
                Order.OrderType.BUY,
                Order.OrderStatus.COMPLETED
        );
        if (totalCash == null) totalCash = BigDecimal.ZERO;

        // 2. 현재 평가 금액 계산
        List<Holdings> holdings = holdingsRepository.findByUser_UserId(userId);
        BigDecimal evaluation = BigDecimal.ZERO;

        for (Holdings h : holdings) {
            String stockCode = h.getStock().getStockCode();
            Optional<StockDetail> optional = stockDetailRepository.findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode);

            if (optional.isPresent()) {
                BigDecimal closePrice = optional.get().getClosePrice();
                BigDecimal quantity = BigDecimal.valueOf(h.getQuantity());
                evaluation = evaluation.add(closePrice.multiply(quantity));
            }
        }

        // 3. 수익 & ROI 계산
        BigDecimal profit = evaluation.subtract(totalCash);
        double roi = totalCash.compareTo(BigDecimal.ZERO) == 0 ?
                0.0 : profit.divide(totalCash, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();

        return new UserInvestmentSummaryDto(
                totalCash.longValue(),
                profit.longValue(),
                Math.round(roi * 100.0) / 100.0  // 소수점 둘째 자리
        );
    }
    public List<HoldingRatioDto> getHoldingRatio(Long userId) {
        List<Holdings> holdings = holdingsRepository.findByUser_UserId(userId);

        // 평가 금액 계산
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<String, BigDecimal> stockValueMap = new HashMap<>();

        for (Holdings h : holdings) {
            String stockName = h.getStock().getStockName();
            String stockCode = h.getStock().getStockCode();

            Optional<StockDetail> optional = stockDetailRepository
                    .findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode);

            if (optional.isPresent()) {
                BigDecimal closePrice = optional.get().getClosePrice();  // ✅ 현재는 종가로 간주
                BigDecimal quantity = BigDecimal.valueOf(h.getQuantity());
                BigDecimal value = closePrice.multiply(quantity);

                stockValueMap.put(stockName,
                        stockValueMap.getOrDefault(stockName, BigDecimal.ZERO).add(value));
                totalValue = totalValue.add(value);
            }
        }

        // 퍼센트 계산
        List<HoldingRatioDto> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : stockValueMap.entrySet()) {
            String stockName = entry.getKey();
            BigDecimal value = entry.getValue();
            double percent = value.multiply(BigDecimal.valueOf(100))
                    .divide(totalValue, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            String color = getColorByStockName(stockName); // 색상 매핑
            result.add(new HoldingRatioDto(stockName, percent, color));
        }

        return result;
    }

    private String getColorByStockName(String stockName) {
        return switch (stockName) {
            case "삼성전자" -> "#36A2EB";
            case "하이닉스" -> "#FFCE56";
            default -> "#AAAAAA";
        };
    }
}