package com.clsa.app.model;

import lombok.Data;

@Data
public class PublishedMarketData {
    public PublishedMarketData(MarketData marketData){
        this.marketData = marketData;
    }

    private MarketData marketData;
    private boolean isPublished;
}
