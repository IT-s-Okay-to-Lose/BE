package com.example.iotl.controller;

import com.example.iotl.entity.StockDetail;
import com.example.iotl.repository.StockRepository;
import com.example.iotl.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final StockRepository stockRepository;

    @PostMapping("/kis/{code}")
    public ResponseEntity<String> saveStock(@PathVariable String code) {
        stockService.saveStockPrice(code);
        return ResponseEntity.ok("Saved.");
    }

    @GetMapping("/kis/{code}")
    public ResponseEntity<StockDetail> getStock(@PathVariable String code) {
        List<StockDetail> list = stockRepository.findByStockCode(code);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list.get(0));
    }

    @GetMapping
    public ResponseEntity<List<StockDetail>> getAll() {
        return ResponseEntity.ok(stockRepository.findAll());
    }

    @GetMapping("/{code}")
    public ResponseEntity<List<StockDetail>> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(stockRepository.findByStockCode(code));
    }
}