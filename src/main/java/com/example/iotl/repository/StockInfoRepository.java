package com.example.iotl.repository;

import com.example.iotl.entity.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockInfoRepository extends JpaRepository<StockInfo, String> {

    @Query("SELECT s.stockCode FROM StockInfo s")
    List<String> findAllStockCodes();  // ✅ 모든 종목 코드 조회
}