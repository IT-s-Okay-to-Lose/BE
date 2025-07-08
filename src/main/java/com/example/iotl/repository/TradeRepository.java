package com.example.iotl.repository;

import com.example.iotl.entity.Trade;
import com.example.iotl.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade,Long> {


    List<Trade> findByOrder_UserAndOrder_Stock_StockCode(User user, String stockCode);


    @Query("SELECT t FROM Trade t " +
        "WHERE t.order.user.userId = :userId " +
        "AND t.executedAt BETWEEN :start AND :end")
    List<Trade> findTradesByUserAndDateRange(@Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

}
