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

        if (marketIndexRepository.existsByIndexNameAndDate(name, LocalDate.now()))
            return false;

        try {
            CurrentIndexResponseDto response = marketIndexApiService.fetchIndex(marketType);
            var data = response.getOutput();

            MarketIndex entity = MarketIndex.builder()
                    .indexName(name)
                    .currentValue(data.getBstp_nmix_prpr())
                    .changeAmount(data.getBstp_nmix_prdy_vrss())
                    .changeRate(data.getBstp_nmix_prdy_ctrt())
                    .changeDirection("1".equals(data.getPrdy_vrss_sign()) ? "▲" : "▼")
                    .date(LocalDate.now())
                    .build();

            marketIndexRepository.save(entity);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}