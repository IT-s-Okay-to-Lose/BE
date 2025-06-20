package com.example.iotl.repository;

import com.example.iotl.entity.Trade;
import com.example.iotl.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade,Long> {


    List<Trade> findByOrder_UserAndOrder_Stock_StockCode(User user, String stockCode);


}
