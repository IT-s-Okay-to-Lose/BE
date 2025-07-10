package com.example.iotl.controller;

import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.service.HogaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hoga")
public class HogaController {

    private final HogaService hogaService;

    @GetMapping
    public ResponseEntity<List<HogaDto>> getHogas(@RequestParam("code") String stockCode) {
        List<HogaDto> hogas = hogaService.getHogas(stockCode);
        return ResponseEntity.ok(hogas);
    }
}
