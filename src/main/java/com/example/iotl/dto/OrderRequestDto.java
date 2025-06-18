package com.example.iotl.dto;

import com.example.iotl.entity.Order.OrderType;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {

    private Long userId;
    private String StockCode;
    private int quantity;
    private OrderType orderType;
    private BigDecimal price;

}
