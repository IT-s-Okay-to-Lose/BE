package com.example.iotl.dto.realized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RealizedProfitDetailDateDto {
    private String date;
    private List<RealizedProfitDetailDto> items;
}
