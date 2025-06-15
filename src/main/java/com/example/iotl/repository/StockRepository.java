package com.example.iotl.repository;

import com.example.iotl.entity.StockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDetail, Long> {
    List<StockDetail> findByStockCode(String stockCode);
}
