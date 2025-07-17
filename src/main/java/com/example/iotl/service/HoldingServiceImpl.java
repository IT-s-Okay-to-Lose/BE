package com.example.iotl.service;

import com.example.iotl.dto.holding.HoldingSummaryDto;
import com.example.iotl.dto.holding.MyStockSummaryDto;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.User;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.StockDetailRepository;
import com.example.iotl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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

    @Override
    public List<HoldingSummaryDto> getMyHoldings(String username) {
        List<Holdings> holdingsList = holdingsRepository.findByUser_username(username);

        return holdingsList.stream()
                .map(h -> {
                    String stockCode = h.getStock().getStockCode();
                    String stockName = h.getStock().getStockName();
                    String stockImageUrl = h.getStock().getLogoUrl();

                    BigDecimal avgBuyPrice = h.getAverageBuyPrice();
                    int quantity = h.getQuantity();

                    BigDecimal currentPrice = stockDetailRepository
                            .findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode)
                            .map(StockDetail::getClosePrice)
                            .orElse(BigDecimal.ZERO);

                    BigDecimal changeRate = BigDecimal.ZERO;
                    if (avgBuyPrice.compareTo(BigDecimal.ZERO) > 0) {
                        changeRate = currentPrice.subtract(avgBuyPrice)
                                .divide(avgBuyPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                    }

                    return HoldingSummaryDto.builder()
                            .stockName(stockName)
                            .stockImageUrl(stockImageUrl)
                            .quantity(quantity)
                            .avgBuyPrice(avgBuyPrice.doubleValue())
                            .changeRate(changeRate.setScale(2, RoundingMode.HALF_UP).doubleValue())
                            .build();
                })
                .toList();
    }
    UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public List<HoldingSummaryDto> getHoldingsByUserName(String name) {
        List<Holdings> holdings = holdingsRepository.findByUser_Name(name);

        return holdings.stream().map(h -> {
            String stockCode = h.getStock().getStockCode();
            BigDecimal currentPrice = stockDetailRepository
                    .findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode)
                    .map(StockDetail::getClosePrice)
                    .orElse(BigDecimal.ZERO);

            double changeRate = 0.0;
            if (h.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                changeRate = currentPrice.subtract(h.getAverageBuyPrice())
                        .divide(h.getAverageBuyPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            return HoldingSummaryDto.builder()
                    .stockName(h.getStock().getStockName())
                    .stockImageUrl(h.getStock().getLogoUrl())
                    .quantity(h.getQuantity())
                    .avgBuyPrice(h.getAverageBuyPrice().doubleValue())
                    .changeRate(changeRate)
                    .build();
        }).toList();
    }
}
