package com.example.iotl.controller;

import com.example.iotl.dto.*;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.repository.StockRepository;
import com.example.iotl.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final StockRepository stockRepository;

    // 특정 종목 코드를 기반으로 KIS API에서 실시간 주식 데이터를 조회하고 저장
    @PostMapping("/kis/{code}")
    public ResponseEntity<String> saveStock(@PathVariable String code) {
        stockService.saveStockPrice(code);
        return ResponseEntity.ok("Saved.");
    }

    // 특정 종목 코드에 대한 가장 최근의 주식 상세 정보 반환
    @GetMapping("/kis/{code}")
    public ResponseEntity<StockDetail> getStock(@PathVariable String code) {
        List<StockDetail> list = stockRepository.findByStockCode(code);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list.get(0));
    }

    // 모든 주식 상세 정보 리스트 조회
    @GetMapping
    public ResponseEntity<List<StockDetail>> getAll() {
        return ResponseEntity.ok(stockRepository.findAll());
    }

    // 특정 종목 코드로 조회된 모든 주식 상세 정보 리스트 반환
    @GetMapping("/{code}")
    public ResponseEntity<List<StockDetail>> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(stockRepository.findByStockCode(code));
    }

    // MarketStockDto 리스트 반환 (메인 페이지 실시간 차트)
    @GetMapping("/market")
    public ResponseEntity<List<MarketStockDto>> getMarketStocks() {
        List<StockDetail> stocks = stockRepository.findAll();
        List<MarketStockDto> result = stocks.stream()
                .map(MarketStockDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // MarketStockInfoDto (디테일 페이지 - 종목 정보)
    @GetMapping("/{code}/info")
    public ResponseEntity<MarketStockInfoDto> getStockInfo(@PathVariable String code) {
        List<StockDetail> stocks = stockRepository.findByStockCode(code);
        return stocks.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(new MarketStockInfoDto(stocks.get(0)));
    }

    // MarketStockPriceInfoDto (디테일 페이지 - 가격 정보)
    @GetMapping("/{code}/price")
    public ResponseEntity<MarketStockPriceInfoDto> getPriceInfo(@PathVariable String code) {
        List<StockDetail> stocks = stockRepository.findByStockCode(code);
        return stocks.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(new MarketStockPriceInfoDto(stocks.get(0)));
    }

    // CandleDataDto (디테일 페이지 - 캔들 차트)
    @GetMapping("/{code}/candle")
    public ResponseEntity<List<CandleDataDto>> getCandleData(@PathVariable String code) {
        List<StockDetail> stocks = stockRepository.findByStockCode(code);
        List<CandleDataDto> result = stocks.stream()
                .map(CandleDataDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // VolumeDataDto (디테일 페이지 - 거래량 차트)
    @GetMapping("/{code}/volume")
    public ResponseEntity<List<VolumeDataDto>> getVolumeData(@PathVariable String code) {
        List<StockDetail> stocks = stockRepository.findByStockCode(code);
        List<VolumeDataDto> result = stocks.stream()
                .map(VolumeDataDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // StockPortfolioDto (내 모의투자 보유 주식)
    @GetMapping("/portfolio")
    public ResponseEntity<List<StockPortfolioDto>> getPortfolio() {
        List<StockDetail> stocks = stockRepository.findAll(); // 실제는 로그인 유저 기반으로 필터링
        List<StockPortfolioDto> result = stocks.stream()
                .map(s -> new StockPortfolioDto(s, 10, s.getClosePrice())) // 예시: 수량 10, 평균가 = 현재가
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}