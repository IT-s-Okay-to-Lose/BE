package com.example.iotl.repository;

import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface HoldingsRepository extends JpaRepository<Holdings,Long> {
    List<Holdings> findByUser_UserId(Long userId);
    Optional<Holdings> findByUser_UserIdAndStock_StockCode(Long userId, String stockCode);
    Optional<Holdings> findByUserAndStock(User user, Stocks stock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Holdings h where h.user = :user and h.stock = :stock")
    Optional<Holdings> findByUserAndStockWithPessimisticLock(@Param("user") User user, @Param("stock") Stocks stock);

}
