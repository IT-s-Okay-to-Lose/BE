package com.example.iotl.repository;

import com.example.iotl.entity.StockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDetail, Long> {

    // 종목 코드로 전체 데이터 (캔들차트 등에서 사용)
    List<StockDetail> findByStockCode(String code);

    // ✅ 가장 최신 데이터 1건만 반환
    StockDetail findTop1ByStockCodeOrderByCreatedAtDesc(String stockCode);
}