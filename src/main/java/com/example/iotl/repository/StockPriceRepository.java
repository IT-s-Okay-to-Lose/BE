package com.example.iotl.repository;

import com.example.iotl.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, String> {
}
