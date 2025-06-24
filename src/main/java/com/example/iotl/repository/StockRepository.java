package com.example.iotl.repository;

import com.example.iotl.entity.StockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDetail, Long> {

    // 종목 코드로 전체 데이터 (캔들차트 등에서 사용)
    List<StockDetail> findByStockCode(String code);

    // ✅ 가장 최신 데이터 1건만 반환
    StockDetail findTop1ByStockCodeOrderByCreatedAtDesc(String stockCode);

    // 해당 종목의 1시간 이전부터의 데이터
    List<StockDetail> findByStockCodeAndCreatedAtBetween(String code, LocalDateTime start, LocalDateTime end);
}