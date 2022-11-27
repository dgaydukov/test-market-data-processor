package com.clsa.app;

import com.clsa.app.model.MarketData;

public class App {
    public static void main(String[] args) {
        MarketDataProcessor mdp = new MarketDataProcessor();
        new Thread(()->{
            mdp.startPublisher(100, 1000);
        }).start();
        new Thread(()->{
            mdp.onMessage(new MarketData("APPL", 100, System.currentTimeMillis()));
        }).start();
    }
}
