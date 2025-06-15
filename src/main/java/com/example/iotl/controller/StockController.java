package com.example.iotl.controller;

import com.example.iotl.dto.StockPriceDto;
import com.example.iotl.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/kis/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/{code}")
    public ResponseEntity<String> saveStock(@PathVariable String code) {
        stockService.saveStockPrice(code);
        return ResponseEntity.ok("Stock price saved successfully.");
    }

    @GetMapping("/{code}")
    public ResponseEntity<StockPriceDto> getAndSaveStock(@PathVariable String code) {
        Map<String, String> output = (Map<String, String>) stockService.getStockPrice(code).get("output");

        if (output == null || output.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 저장 로직 수행
        stockService.saveStockPrice(code);

        // 클라이언트 응답용 DTO 구성
        StockPriceDto dto = new StockPriceDto();
        dto.setStockCode(output.get("stck_shrn_iscd"));
        dto.setOpenPrice(new BigDecimal(output.get("stck_oprc")));
        dto.setHighPrice(new BigDecimal(output.get("stck_hgpr")));
        dto.setLowPrice(new BigDecimal(output.get("stck_lwpr")));
        dto.setClosePrice(new BigDecimal(output.get("stck_prpr")));
        dto.setPriceDiff(new BigDecimal(output.get("prdy_vrss")));
        dto.setPriceRate(new BigDecimal(output.get("prdy_ctrt")));
        dto.setPriceSign(Byte.parseByte(output.get("prdy_vrss_sign")));
        dto.setVolume(Long.parseLong(output.get("acml_vol")));
        dto.setPrevClosePrice(dto.getClosePrice().subtract(dto.getPriceDiff()));
        dto.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(dto);
    }
}