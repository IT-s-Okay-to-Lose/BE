package com.example.iotl.service;

import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.domain.hoga.HogaGenerator;
import com.example.iotl.repository.StockDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HogaService {

    private final StockDetailRepository stockDetailRepository;

    public List<HogaDto> getHogas(String stockCode) {
        // 가장 최신 closePrice (현재가) 가져오기
        BigDecimal closePrice = stockDetailRepository
            .findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode)
            .map(detail -> detail.getClosePrice())
            .orElseThrow(() -> new IllegalArgumentException("해당 종목의 현재가 데이터가 없습니다: " + stockCode));

        // 현재가를 int로 변환해서 generator에 넘김
        int currentPrice = closePrice.intValue();

        return HogaGenerator.generate(currentPrice);
    }
}
