package com.example.iotl.service.marketIndex;

import com.example.iotl.dto.marketindex.CurrentIndexResponseDto;
import com.example.iotl.dto.marketindex.MarketIndexDto;
import com.example.iotl.entity.MarketIndex;
import com.example.iotl.repository.MarketIndexRepository;
import com.example.iotl.service.stock.StockApiService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MarketIndexService {

    private final MarketIndexRepository marketIndexRepository;
    private final MarketIndexApiService marketIndexApiService;

    public MarketIndexDto getToday(String marketType) {
        String name = marketType.equalsIgnoreCase("KOSPI") ? "코스피" : "코스닥";
        return marketIndexRepository.findByIndexNameAndDate(name, LocalDate.now())
                .map(MarketIndexDto::fromEntity)
                .orElse(null);
    }

    public boolean saveIfAbsent(String marketType) {
        String name = marketType.equalsIgnoreCase("KOSPI") ? "코스피" : "코스닥";
        LocalDate today = LocalDate.now();

        if (marketIndexRepository.existsByIndexNameAndDate(name, today))
            return false;

        try {
            CurrentIndexResponseDto response = marketIndexApiService.fetchIndex(marketType);
            var data = response.getOutput();

            double todayValue = Double.parseDouble(String.valueOf(data.getBstp_nmix_prpr()));

            // 전날 값 가져오기
            MarketIndex yesterday = marketIndexRepository
                    .findTopByIndexNameAndDateBeforeOrderByDateDesc(name, today)
                    .orElse(null);

            double prevValue = yesterday != null ? yesterday.getCurrentValue() : todayValue;

            double changeAmountRaw = todayValue - prevValue;
            double changeRateRaw = (prevValue != 0) ? (changeAmountRaw / prevValue * 100) : 0.0;

            // ✅ 소수 둘째 자리까지 반올림
            double changeAmount = Math.round(changeAmountRaw * 100.0) / 100.0;
            double changeRate = Math.round(changeRateRaw * 100.0) / 100.0;

            String changeDirection = changeAmount > 0 ? "▲" : (changeAmount < 0 ? "▼" : "-");

            MarketIndex entity = MarketIndex.builder()
                    .indexName(name)
                    .currentValue(todayValue)
                    .changeAmount(changeAmount)
                    .changeRate(changeRate)
                    .changeDirection(changeDirection)
                    .date(today)
                    .build();

            marketIndexRepository.save(entity);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}