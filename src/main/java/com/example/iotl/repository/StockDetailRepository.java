package com.example.iotl.repository;


import com.example.iotl.entity.StockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 주가, 시세, 차트 등 시세 데이터
@Repository
public interface StockDetailRepository extends JpaRepository<StockDetail, Long> {
    // 가장 최신 closePrice 하나 가져오기
    Optional<StockDetail> findTopByStocks_StockCodeOrderByCreatedAtDesc(String stockCode);
}