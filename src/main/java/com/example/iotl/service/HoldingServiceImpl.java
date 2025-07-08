package com.example.iotl.service;

import com.example.iotl.dto.holding.MyStockSummaryDto;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.StockDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingsRepository holdingsRepository;
    private final StockDetailRepository stockDetailRepository;



    @Override
    public MyStockSummaryDto getMyStockSummary(String userName, String stockCode) {
        Holdings h = holdingsRepository
            .findByUser_UsernameAndStock_StockCode(userName, stockCode)
            .orElseThrow(() -> new RuntimeException("보유 종목이 없습니다."));

        BigDecimal averagePrice = h.getAverageBuyPrice();
        int quantity = h.getQuantity();

        Optional<StockDetail> stockDetailOpt = stockDetailRepository
            .findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode);

        if (stockDetailOpt.isEmpty()) {
            throw new RuntimeException("주식 현재가를 찾을 수 없습니다.");
        }

        BigDecimal currentPrice = stockDetailOpt.get().getClosePrice();
        BigDecimal totalNowAmount = currentPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal fee = totalNowAmount.multiply(new BigDecimal("0.0003"))
            .setScale(0, RoundingMode.HALF_UP);

        BigDecimal profit = currentPrice.subtract(averagePrice)
            .multiply(BigDecimal.valueOf(quantity))
            .subtract(fee);

        return MyStockSummaryDto.builder()
            .averagePrice(averagePrice)
            .quantity(quantity)
            .expectedFee(fee)
            .totalProfit(profit)
            .build();
    }
}
