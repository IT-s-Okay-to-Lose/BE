package com.example.iotl.controller;

import com.example.iotl.dto.profit.ProfitResponse;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/investments")
public class InvestmentController {
    private final BaseResponseService baseResponseService;
    @GetMapping("/profits")
    public ResponseEntity<BaseResponse<ProfitResponse>> getProfitGraph(
            @RequestParam String period
    ){
        List<ProfitResponse.ProfitPoint> points;
        switch (period) {
            case "1day":
                points = getMockDataFor1Day();
                break;
            case "3day":
                points = getMockDataFor3Day();
                break;
            case "1week":
                points = getMockDataFor1Week();
                break;
            case "1month":
                points = getMockDataFor1Month();
                break;
            default:
                // 기본값 또는 예외 처리
                points = getMockDataFor1Day();
                break;
        }

        long totalProfit = points.stream().mapToLong(ProfitResponse.ProfitPoint::getAmount).sum();

        ProfitResponse response = new ProfitResponse();
        response.setTotalProfit(totalProfit);
        response.setPoints(points);

        return ResponseEntity.ok(baseResponseService.getSuccessResponse(response));
    }
    private List<ProfitResponse.ProfitPoint> getMockDataFor1Day() {
        return List.of(
                new ProfitResponse.ProfitPoint("09:00", 10000L),
                new ProfitResponse.ProfitPoint("09:05", 20000L),
                new ProfitResponse.ProfitPoint("09:10", 30000L)
        );
    }
    private List<ProfitResponse.ProfitPoint> getMockDataFor3Day() {
        return List.of(
                new ProfitResponse.ProfitPoint("07.14", 120000L),
                new ProfitResponse.ProfitPoint("07.15", 80000L),
                new ProfitResponse.ProfitPoint("07.16", 100000L)
        );
    }
    private List<ProfitResponse.ProfitPoint> getMockDataFor1Week() {
        return List.of(
                new ProfitResponse.ProfitPoint("07.10", 100000L),
                new ProfitResponse.ProfitPoint("07.11", 120000L),
                new ProfitResponse.ProfitPoint("07.12", -80000L),
                new ProfitResponse.ProfitPoint("07.13", 150000L),
                new ProfitResponse.ProfitPoint("07.14", 70000L)
        );
    }
    private List<ProfitResponse.ProfitPoint> getMockDataFor1Month() {
        return List.of(
                new ProfitResponse.ProfitPoint("07.01~07.07", 250000L),
                new ProfitResponse.ProfitPoint("07.08~07.14", 300000L),
                new ProfitResponse.ProfitPoint("07.15~07.21", 180000L),
                new ProfitResponse.ProfitPoint("07.22~07.28", 220000L)
        );
    }
}
