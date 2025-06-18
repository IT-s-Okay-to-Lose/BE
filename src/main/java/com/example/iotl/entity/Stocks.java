package com.example.iotl.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holdings> holdings = new ArrayList<>();

}