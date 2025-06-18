package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
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
import java.util.List;
import java.util.Optional;

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
}