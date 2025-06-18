package com.example.iotl.repository;

import com.example.iotl.entity.Holdings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldingsRepository extends JpaRepository<Holdings,Long> {
    List<Holdings> findByUser_UserId(Long userId);
}
