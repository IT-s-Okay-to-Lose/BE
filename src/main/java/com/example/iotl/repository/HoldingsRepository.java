package com.example.iotl.repository;

import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingsRepository extends JpaRepository<Holdings,Long> {

    Optional<Holdings> findByUserAndStock(User user, Stocks stock);

}
