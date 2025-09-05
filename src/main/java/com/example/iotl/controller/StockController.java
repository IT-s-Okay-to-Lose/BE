package com.example.iotl.controller;

import com.example.iotl.dto.stocks.*;
import com.example.iotl.entity.Stocks;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
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
    private final BaseResponseService baseResponseService;

    @GetMapping("/meta")
    @Operation(summary = "모든 종목 정적 데이터 조회")
    public ResponseEntity<BaseResponse<List<StaticStockMetaDto>>> getAllStockMetas() {
        List<StaticStockMetaDto> data = stockService.getAllStockMetas();
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }

    @GetMapping("/dynamic")
    @Operation(summary = "전체 종목의 실시간 동적 데이터 조회")
    public ResponseEntity<BaseResponse<List<DynamicStockDataDto>>> getAllDynamicStocks() {
        List<DynamicStockDataDto> data = stockService.getAllDynamicStocks();
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }

    @GetMapping("/{code}/volume")
    @Operation(summary = "거래량 차트 데이터 조회")
    public ResponseEntity<BaseResponse<List<List<Object>>>> getVolumeData(@PathVariable String code) {
        List<List<Object>> data = toTimeSeries(
                stockService.findStocksByCode(code).stream().map(VolumeDataDto::from).toList(),
                v -> List.of(v.getTime().toString(), v.getVolume())
        );
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }

    @GetMapping("/{code}/candle")
    @Operation(summary = "캔들 차트 데이터 조회")
    public ResponseEntity<BaseResponse<List<List<Object>>>> getCandleData(@PathVariable String code) {
        List<List<Object>> data = toTimeSeries(
                stockService.findStocksByCode(code).stream().map(CandleDataDto::from).toList(),
                c -> List.of(
                        c.getTime() != null ? c.getTime().toString() : null,
                        c.getOpen(), c.getHigh(), c.getLow(), c.getClose()
                )
        );
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }

    @GetMapping("/{code}/meta")
    @Operation(summary = "종목 메타 정보 조회")
    public ResponseEntity<BaseResponse<StaticStockMetaDto>> getStockMeta(@PathVariable String code) {
        Stocks stock = stockService.findStockByStockCode(code);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(StaticStockMetaDto.from(stock)));
    }

    @GetMapping
    @Operation(summary = "전체 종목 상세 정보 조회")
    public ResponseEntity<BaseResponse<List<StockDetailDto>>> getAll() {
        List<StockDetailDto> data = stockService.findAllStocks().stream()
                .map(StockDetailDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }

    @GetMapping("/{code}")
    @Operation(summary = "특정 종목코드로 한 종목에 대한 상세 정보 조회")
    public ResponseEntity<BaseResponse<List<StockDetailDto>>> findByCode(@PathVariable String code) {
        List<StockDetailDto> data = stockService.findStocksByCode(code).stream()
                .map(StockDetailDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(data));
    }

    @GetMapping("/{code}/marketinfo")
    @Operation(summary = "한 종목에 대한 현재가, 어제 대비 가격차이, 어제 대비 등락률 조회")
    public ResponseEntity<BaseResponse<MarketStockPriceInfoDto>> getPriceInfo(@PathVariable String code) {
        return toResponse(
                Optional.ofNullable(stockService.findLatestStockByCode(code))
                        .map(MarketStockPriceInfoDto::from)
        );
    }

    @GetMapping("/{code}/dynamic")
    @Operation(summary = "한 종목 실시간 동적 주식 정보 조회")
    public ResponseEntity<BaseResponse<DynamicStockDataDto>> getDynamicStockData(@PathVariable String code) {
        return toResponse(
                Optional.ofNullable(stockService.findLatestStockByCode(code))
                        .map(DynamicStockDataDto::from)
        );
    }

    @PostMapping("/kis/{code}")
    @Operation(summary = "특정 종목 실시간 저장")
    public ResponseEntity<BaseResponse<String>> saveStock(@PathVariable String code) {
        return Optional.ofNullable(stockService.saveStockPrice(code))
                .map(s -> ResponseEntity.ok(baseResponseService.getSuccessResponse("Saved.")))
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body(baseResponseService.getFailureResponse("Stock not saved.", 500)));
    }

    @GetMapping("/kis/{code}")
    @Operation(summary = "특정 종목 상세 정보 조회")
    public ResponseEntity<BaseResponse<StockDetailDto>> getStock(@PathVariable String code) {
        return toResponse(
                Optional.ofNullable(stockService.findLatestStockByCode(code))
                        .map(StockDetailDto::from)
        );
    }

    // ================= 유틸 ==================

    private <T> List<List<Object>> toTimeSeries(List<T> list, Function<T, List<Object>> rowMapper) {
        return list.stream().map(rowMapper).collect(Collectors.toList());
    }

    private <T> ResponseEntity<BaseResponse<T>> toResponse(Optional<T> optionalDto) {
        return optionalDto
                .map(dto -> ResponseEntity.ok(baseResponseService.getSuccessResponse(dto)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(baseResponseService.getFailureResponse("데이터를 찾을 수 없습니다.", 404)));
    }
}