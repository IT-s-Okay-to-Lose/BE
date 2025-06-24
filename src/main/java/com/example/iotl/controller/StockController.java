package com.example.iotl.controller;

import com.example.iotl.dto.stocks.*;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*") // 또는 허용할 도메인만 지정
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "주식 관련 API")
public class StockController {

    private final StockService stockService;

    @Operation(summary = "특정 종목 실시간 저장", description = "KIS API를 호출하여 해당 종목 코드를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/kis/{code}")
    public ResponseEntity<String> saveStock(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        stockService.saveStockPrice(code);
        return ResponseEntity.ok("Saved.");
    }

    @Operation(summary = "특정 종목 상세 정보 조회", description = "최근 저장된 StockDetail을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "종목 정보를 찾을 수 없음")
    })
    @GetMapping("/kis/{code}")
    public ResponseEntity<StockDetailDto> getStock(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        var stock = stockService.findLatestStockByCode(code);
        return stock == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(new StockDetailDto(stock));
    }

    @Operation(summary = "전체 종목 상세 정보 조회", description = "모든 주식 상세 정보 리스트를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<StockDetailDto>> getAll() {
        return ResponseEntity.ok(
                stockService.findAllStocks().stream()
                        .map(StockDetailDto::new)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "특정 종목코드로 한 종목에 대한 상세 정보 조회", description = "코드 기반으로 상세정보를 리스트로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{code}")
    public ResponseEntity<List<StockDetailDto>> findByCode(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        return ResponseEntity.ok(
                stockService.findStocksByCode(code).stream()
                        .map(StockDetailDto::new)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "실시간 동적 주식 정보 조회", description = "DynamicStockDataDto 반환 (현재가, 등락률, 거래량)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 종목 없음")
    })
    @GetMapping("/{code}/dynamic")
    public ResponseEntity<DynamicStockDataDto> getDynamicStockData(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        StockDetail stock = stockService.findLatestStockByCode(code);
        return stock == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(new DynamicStockDataDto(stock));
    }

    @Operation(summary = "다중 종목 실시간 동적 데이터 조회", description = "동적 데이터(DynamicStockDataDto) 리스트 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/dynamic")
    public ResponseEntity<List<DynamicStockDataDto>> getDynamicStocksByCodes(
            @Parameter(description = "조회할 종목 코드 리스트", example = "005930,000660,207940")
            @RequestParam List<String> codes) {

        List<StockDetail> latestList = stockService.findLatestStocksByCodes(codes);
        List<DynamicStockDataDto> dtoList = latestList.stream()
                .map(DynamicStockDataDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "모든 종목 정적 데이터 조회", description = "StaticStockMetaDto 리스트 반환 (정적 종목 정보)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/meta")
    public ResponseEntity<List<StaticStockMetaDto>> getAllStockMetas() {
        return ResponseEntity.ok(stockService.getAllStockMetas());
    }

    @Operation(summary = "메인페이지 실시간 차트 데이터", description = "MarketStockDto 리스트 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/market")
    public ResponseEntity<List<MarketStockDto>> getMarketStocks() {
        return ResponseEntity.ok(
                stockService.findAllStocks().stream()
                        .map(MarketStockDto::new)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "상세 페이지 종목 정보 조회", description = "MarketStockInfoDto 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 종목 없음")
    })
    @GetMapping("/{code}/info")
    public ResponseEntity<MarketStockInfoDto> getStockInfo(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        var stock = stockService.findLatestStockByCode(code);
        return stock == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(new MarketStockInfoDto(stock));
    }

    @Operation(summary = "상세 페이지 가격 정보 조회", description = "MarketStockPriceInfoDto 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 종목 없음")
    })
    @GetMapping("/{code}/price")
    public ResponseEntity<MarketStockPriceInfoDto> getPriceInfo(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        var stock = stockService.findLatestStockByCode(code);
        return stock == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(new MarketStockPriceInfoDto(stock));
    }

    @Operation(summary = "캔들 차트 데이터 조회", description = "요청 시각 기준 1시간 전부터 현재까지의 CandleDataDto 리스트 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{code}/candle")
    public ResponseEntity<List<CandleDataDto>> getCandleData(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        return ResponseEntity.ok(
                stockService.findStocksWithinOneHour(code).stream()
                        .map(CandleDataDto::new)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "거래량 차트 데이터 조회", description = "VolumeDataDto 리스트 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{code}/volume")
    public ResponseEntity<List<VolumeDataDto>> getVolumeData(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code) {
        return ResponseEntity.ok(
                stockService.findStocksByCode(code).stream()
                        .map(VolumeDataDto::new)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "내 보유 주식 목록 조회", description = "StockPortfolioDto 리스트 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/portfolio")
    public ResponseEntity<List<StockPortfolioDto>> getPortfolio() {
        return ResponseEntity.ok(
                stockService.findAllStocks().stream()
                        .map(s -> new StockPortfolioDto(s, 10, s.getClosePrice()))
                        .collect(Collectors.toList())
        );
    }
}