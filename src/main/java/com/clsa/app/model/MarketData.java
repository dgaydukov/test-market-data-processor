package com.clsa.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketData {
    private String symbol;
    private int price;
    private long updateTime;
}
