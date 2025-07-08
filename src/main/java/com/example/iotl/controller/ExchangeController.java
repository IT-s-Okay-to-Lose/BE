package com.example.iotl.controller;

import com.example.iotl.dto.exchange.ExchangeSummaryDto;
import com.example.iotl.service.exchange.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping
    public ResponseEntity<ExchangeSummaryDto> getExchangeSummary() {
        LocalDate today = LocalDate.now();

        return exchangeService.getExchangeSummary(today)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    boolean saved = exchangeService.saveTodayExchangeIfAbsent();
                    return exchangeService.getExchangeSummary(today)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
                });
    }
}