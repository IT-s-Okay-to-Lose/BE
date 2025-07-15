package com.example.iotl.controller;

import com.example.iotl.dto.stocks.*;
import com.example.iotl.entity.Stocks;
import com.example.iotl.service.stock.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/api/stocks")
@Tag(name = "Stocks", description = "주식 관련 API")
public class StockController {

    private final StockService stockService;

    @GetMapping("/meta")
    @Operation(summary = "모든 종목 정적 데이터 조회")
    public List<StaticStockMetaDto> getAllStockMetas() {
        return stockService.getAllStockMetas();
    }

    @GetMapping("/dynamic")
    @Operation(summary = "전체 종목의 실시간 동적 데이터 조회")
    public List<DynamicStockDataDto> getAllDynamicStocks() {
        return stockService.getAllDynamicStocks();
    }

    @GetMapping("/{code}/volume")
    @Operation(summary = "거래량 차트 데이터 조회")
    public List<List<Object>> getVolumeData(@PathVariable String code) {
        return toTimeSeries(
                stockService.findStocksByCode(code).stream().map(VolumeDataDto::from).toList(),
                v -> List.of(v.getTime().toString(), v.getVolume())
        );
    }

    @GetMapping("/{code}/candle")
    @Operation(summary = "캔들 차트 데이터 조회")
    public List<List<Object>> getCandleData(@PathVariable String code) {
        return toTimeSeries(
                stockService.findStocksByCode(code).stream().map(CandleDataDto::from).toList(),
                c -> List.of(
                        c.getTime() != null ? c.getTime().toString() : null,
                        c.getOpen(), c.getHigh(), c.getLow(), c.getClose()
                )
        );
    }

    @GetMapping("/{code}/meta")
    @Operation(summary = "종목 메타 정보 조회")
    public StaticStockMetaDto getStockMeta(@PathVariable String code) {
        Stocks stock = stockService.findStockByStockCode(code);
        return StaticStockMetaDto.from(stock); // 예: 정적 팩토리 메서드
    }

    @GetMapping
    @Operation(summary = "전체 종목 상세 정보 조회")
    public List<StockDetailDto> getAll() {
        return stockService.findAllStocks().stream()
                .map(StockDetailDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{code}")
    @Operation(summary = "특정 종목코드로 한 종목에 대한 상세 정보 조회")
    public List<StockDetailDto> findByCode(@PathVariable String code) {
        return stockService.findStocksByCode(code).stream()
                .map(StockDetailDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{code}/marketinfo")
    @Operation(summary = "한 종목에 대한 현재가, 어제 대비 가격차이, 어제 대비 등락률 조회")
    public ResponseEntity<MarketStockPriceInfoDto> getPriceInfo(@PathVariable String code) {
        return toResponse(
                Optional.ofNullable(stockService.findLatestStockByCode(code))
                        .map(MarketStockPriceInfoDto::from)
        );
    }

    @GetMapping("/{code}/dynamic")
    @Operation(summary = "한 종목 실시간 동적 주식 정보 조회")
    public ResponseEntity<DynamicStockDataDto> getDynamicStockData(@PathVariable String code) {
        return toResponse(
                Optional.ofNullable(stockService.findLatestStockByCode(code))
                        .map(DynamicStockDataDto::from)
        );
    }

    @PostMapping("/kis/{code}")
    @Operation(summary = "특정 종목 실시간 저장")
    public ResponseEntity<String> saveStock(@PathVariable String code) {
        return Optional.ofNullable(stockService.saveStockPrice(code))
                .map(s -> ResponseEntity.ok("Saved."))
                .orElseGet(() -> ResponseEntity.internalServerError().body("Stock not saved."));
    }

    @GetMapping("/kis/{code}")
    @Operation(summary = "특정 종목 상세 정보 조회")
    public ResponseEntity<StockDetailDto> getStock(@PathVariable String code) {
        return toResponse(
                Optional.ofNullable(stockService.findLatestStockByCode(code))
                        .map(StockDetailDto::from)
        );
    }

    // ================= 유틸 ==================

    private <T> List<List<Object>> toTimeSeries(List<T> list, Function<T, List<Object>> rowMapper) {
        return list.stream().map(rowMapper).collect(Collectors.toList());
    }

    private <T> ResponseEntity<T> toResponse(Optional<T> optionalDto) {
        return optionalDto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}