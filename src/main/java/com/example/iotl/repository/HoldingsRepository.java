package com.example.iotl.repository;

import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface HoldingsRepository extends JpaRepository<Holdings,Long> {
    List<Holdings> findByUser_UserId(Long userId);
    Optional<Holdings> findByUser_UserIdAndStock_StockCode(Long userId, String stockCode);
    Optional<Holdings> findByUserAndStock(User user, Stocks stock);

}
