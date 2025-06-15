package com.example.iotl.repository;

import com.example.iotl.entity.Stocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface StockInfoRepository extends JpaRepository<Stocks, String> {
    @Query("SELECT s.stockCode FROM Stocks s")
    List<String> findAllStockCodes();
}