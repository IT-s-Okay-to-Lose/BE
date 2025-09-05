package com.example.iotl.controller;

import com.example.iotl.dto.marketindex.MarketIndexDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.service.marketIndex.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/api/market-index")
@RequiredArgsConstructor
public class MarketIndexController {

    private final MarketIndexService marketIndexService;
    private final BaseResponseService baseResponseService;

    @GetMapping("/{marketType}")
    public ResponseEntity<BaseResponse<MarketIndexDto>> getToday(@PathVariable String marketType) {
        MarketIndexDto dto = marketIndexService.getToday(marketType);

        if (dto == null) {
            boolean success = marketIndexService.saveIfAbsent(marketType);
            if (!success) {
                return ResponseEntity.status(502).body(
                        baseResponseService.getFailureResponse("외부 API 호출에 실패했습니다.", 502)
                );
            }

            dto = marketIndexService.getToday(marketType);
            if (dto == null) {
                return ResponseEntity.status(204).body(
                        baseResponseService.getFailureResponse("마켓 지수 저장에 실패했습니다.", 204)
                );
            }
        }

        return ResponseEntity.ok(baseResponseService.getSuccessResponse(dto));
    }

    @PostMapping("/save")
    public ResponseEntity<BaseResponse<String>> forceSave() {
        boolean kospi = marketIndexService.saveIfAbsent("KOSPI");
        boolean kosdaq = marketIndexService.saveIfAbsent("KOSDAQ");
        String resultMessage = "저장 완료: " + (kospi || kosdaq);

        return ResponseEntity.ok(baseResponseService.getSuccessResponse(resultMessage));
    }
}