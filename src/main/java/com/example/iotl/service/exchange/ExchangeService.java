package com.example.iotl.service.exchange;

import com.example.iotl.dto.exchange.ExchangeRateResponse;
import com.example.iotl.dto.exchange.ExchangeSummaryDto;
import com.example.iotl.entity.Exchange;
import com.example.iotl.repository.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private static final String BASE_CODE = "USD";
    private static final String TARGET_CODE = "KRW";

    private final ExchangeRepository exchangeRepository;
    private final ExchangeApiService exchangeApiService;

    public boolean existsByDate(LocalDate date) {
        return exchangeRepository.existsByBaseCodeAndTargetCodeAndDate(BASE_CODE, TARGET_CODE, date);
    }

    public void saveRate(double rate, LocalDate date) {
        exchangeRepository.save(Exchange.builder()
                .baseCode(BASE_CODE)
                .targetCode(TARGET_CODE)
                .rate(rate)
                .date(date)
                .build());
    }

    public boolean saveTodayExchangeIfAbsent() {
        LocalDate today = LocalDate.now();
        if (existsByDate(today)) return true;

        ExchangeRateResponse response = exchangeApiService.fetch();
        Double krwRate = response.getConversion_rates().get(TARGET_CODE);

        if (krwRate != null) {
            saveRate(krwRate, today);
            return true;
        }

        return false;
    }

    public Optional<ExchangeSummaryDto> getExchangeSummary(LocalDate date) {
        Optional<Exchange> todayOpt = exchangeRepository.findByBaseCodeAndTargetCodeAndDate(BASE_CODE, TARGET_CODE, date);
        if (todayOpt.isEmpty()) return Optional.empty();

        Exchange today = todayOpt.get();
        Optional<Exchange> yesterdayOpt = exchangeRepository.findByBaseCodeAndTargetCodeAndDate(BASE_CODE, TARGET_CODE, date.minusDays(1));

        double diff = 0;
        double percent = 0;

        if (yesterdayOpt.isPresent()) {
            Exchange yesterday = yesterdayOpt.get();
            diff = roundTo1Decimal(today.getRate() - yesterday.getRate());
            percent = roundTo2Decimal((diff / yesterday.getRate()) * 100);
        }

        return Optional.of(ExchangeSummaryDto.builder()
                .rate(today.getRate())
                .difference(diff)
                .percent(percent)
                .build());
    }

    private double roundTo1Decimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double roundTo2Decimal(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}