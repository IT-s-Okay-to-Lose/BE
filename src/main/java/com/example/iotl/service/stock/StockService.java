package com.example.iotl.service.stock;

import com.example.iotl.dto.stocks.DynamicStockDataDto;
import com.example.iotl.dto.stocks.StaticStockMetaDto;
import com.example.iotl.dto.stocks.StockDetailDto;
import com.example.iotl.dto.stocks.StockPriceDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.Stocks;
import com.example.iotl.repository.StockDetailRepository;
import com.example.iotl.repository.StocksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockApiService stockApiService;
    private final StockDetailRepository stockDetailRepository;
    private final StocksRepository stocksRepository;

    public StockPriceDto saveStockPrice(String code) {
        Map<String, Object> result = stockApiService.getStockPrice(code);
        Map<String, String> output = (Map<String, String>) result.get("output");

        Stocks stocks = stocksRepository.findById(code).orElse(null);
        if (stocks == null) return null;

        StockDetailDto dto = StockDetailDto.fromOutput(output);
        StockDetail saved = stockDetailRepository.save(dto.toEntity(stocks));

        return StockPriceDto.from(saved);
    }

    public List<StockDetail> findStocksByCode(String code) {
        return stockDetailRepository.findByStockCode(code);
    }

    public Stocks findStockByStockCode(String code) {
        return stocksRepository.findStockByStockCode(code);
    }

    public StockDetail findLatestStockByCode(String code) {
        return stockDetailRepository.findTop1ByStockCodeOrderByCreatedAtDesc(code);
    }

    public List<StockDetail> findAllStocks() {
        return stockDetailRepository.findAll();
    }

    public List<StaticStockMetaDto> getAllStockMetas() {
        return stocksRepository.findAll().stream()
                .map(StaticStockMetaDto::from)
                .collect(Collectors.toList());
    }

    public List<DynamicStockDataDto> getAllDynamicStocks() {
        List<String> codes = stocksRepository.findAllStockCodes();
        List<StockDetail> latestList = findLatestStocksByCodes(codes);

        return latestList.stream()
                .map(DynamicStockDataDto::from)
                .collect(Collectors.toList());
    }

    public List<StockDetail> findLatestStocksByCodes(List<String> codes) {
        return codes.stream()
                .map(this::findLatestStockByCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}