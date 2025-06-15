package com.example.iotl.service;

import com.example.iotl.entity.StockDetail;
import com.example.iotl.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendService {
    private final StockRepository stockRepository;
    public void saveToDatabase(MultipartFile file) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        boolean isFirst = true;
        List<StockDetail> stockDetails = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (isFirst) {
                isFirst = false;
                continue;
            }
            try {
                String[] tokens = line.split(",", -1);
                if (tokens.length < 12) {
                    System.out.println("필드 부족: " + line);
                    continue;
                }

                StockDetail stockDetail = StockDetail.builder()
                        .stockCode(tokens[1].replace("\"", ""))
                        .openPrice(new BigDecimal(tokens[2].replace("\"", "")))
                        .highPrice(new BigDecimal(tokens[3].replace("\"", "")))
                        .lowPrice(new BigDecimal(tokens[4].replace("\"", "")))
                        .closePrice(new BigDecimal(tokens[5].replace("\"", "")))
                        .prevClosePrice(new BigDecimal(tokens[6].replace("\"", "")))
                        .priceDiff(new BigDecimal(tokens[7].replace("\"", "")))
                        .priceRate(new BigDecimal(tokens[8].replace("\"", "")))
                        .priceSign(Byte.parseByte(tokens[9].replace("\"", "")))
                        .volume(Long.parseLong(tokens[10].replace("\"", "")))
                        .createdAt(LocalDateTime.parse(tokens[11].replace("\"", ""), formatter))
                        .build();

                stockDetails.add(stockDetail);

            } catch (Exception e) {
                System.out.println("문제 있는 줄: " + line);
                e.printStackTrace();
            }
        }
        System.out.println("저장 대상: " + stockDetails.size() + "건");
        stockRepository.saveAll(stockDetails);
    }
}