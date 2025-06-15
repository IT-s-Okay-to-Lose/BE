package com.example.iotl.controller;

import com.example.iotl.dto.StockDetailDto;
import com.example.iotl.dto.StockPriceDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.StockRepository;
import com.example.iotl.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    // 실시간 가격 조회 및 저장
    @PostMapping("/kis/{code}")
    public ResponseEntity<String> saveStock(@PathVariable String code) {
        stockService.saveStockPrice(code);
        return ResponseEntity.ok("Stock price saved successfully.");
    }

    @GetMapping("/kis/{code}")
    public ResponseEntity<StockPriceDto> getAndSaveStock(@PathVariable String code) {
        Map<String, String> output = (Map<String, String>) stockService.getStockPrice(code).get("output");

        if (output == null || output.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        stockService.saveStockPrice(code);

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

    // 종목 전체 조회
    @GetMapping
    public List<StockDetailDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(StockDetailDto::new)
                .collect(Collectors.toList());
    }

    // 종목 코드로 조회
    @GetMapping("/{code}")
    public List<StockDetail> getStocksByCode(@PathVariable String code) {
        return stockRepository.findByStockCode(code);
    }


    @GetMapping("/market")
    public List<StockDetailDto> getMarketStockList() {
        return stockRepository.findAll().stream()
                .map(StockDetailDto::new)
                .collect(Collectors.toList());
    }
}