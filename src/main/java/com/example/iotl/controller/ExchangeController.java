package com.example.iotl.controller;

import com.example.iotl.dto.exchange.ExchangeSummaryDto;
import com.example.iotl.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping
    public ResponseEntity<ExchangeSummaryDto> getExchangeSummary() {
        try {
            ExchangeSummaryDto dto = exchangeService.getTodayExchangeSummary();
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);  // or custom message
        }
    }
}