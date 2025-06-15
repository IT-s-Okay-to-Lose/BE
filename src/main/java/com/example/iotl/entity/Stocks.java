package com.example.iotl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "stocks")
public class Stocks {

    @Id
    @Column(name = "stock_code", length = 10)
    private String stockCode;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "market_type")
    private String marketType;

    @Column(name = "logo_url", columnDefinition = "LONGTEXT")
    private String logoUrl;
}