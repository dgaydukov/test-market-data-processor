package com.clsa.app;

import com.clsa.app.model.MarketData;
import org.junit.Test;
import org.mockito.Mockito;

public class MarketDataProcessorTest {
    @Test
    public void sendOneSymbolTest() throws InterruptedException {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        mdp.onMessage(new MarketData("APPL", 100, System.currentTimeMillis()));
        mdp.onMessage(new MarketData("APPL", 110, System.currentTimeMillis()));
        MarketData md3 = new MarketData("APPL", 120, System.currentTimeMillis());
        mdp.onMessage(md3);
        new Thread(()->{
            mdp.startPublisher(3, 1000);
        }).start();
        Thread.sleep(100);
        Mockito.verify(mdp).publishAggregatedMarketData(md3);
    }

    @Test
    public void sendTwoSymbolsTest() throws InterruptedException {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        new Thread(()->{
            mdp.startPublisher(3, 1000);
        }).start();
        MarketData md1 = new MarketData("APPL", 100, System.currentTimeMillis());
        mdp.onMessage(md1);
        Thread.sleep(100);
        Mockito.verify(mdp).publishAggregatedMarketData(md1);

        MarketData md2 = new MarketData("APPL", 110, System.currentTimeMillis());
        mdp.onMessage(md2);
        // check that method wasn't called, cause we have to send one symbol once during sliding window
        Mockito.verify(mdp, Mockito.times(0)).publishAggregatedMarketData(md2);
        Thread.sleep(1000);
        // after sliding window is over, we should call it again
        Mockito.verify(mdp).publishAggregatedMarketData(md2);
    }

    @Test
    public void sendManyOutdatedTest() throws InterruptedException {
        final long time = System.currentTimeMillis();
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        new Thread(()->{
            mdp.startPublisher(3, 1000);
        }).start();
        MarketData md1 = new MarketData("APPL", 100, time + 1);
        mdp.onMessage(md1);
        Thread.sleep(100);
        Mockito.verify(mdp).publishAggregatedMarketData(md1);

        for (int i = 0; i < 5; i++){
            mdp.onMessage(new MarketData("APPL", 100 + i, time));
        }
        Thread.sleep(1000);

        Mockito.verify(mdp, Mockito.times(1)).publishAggregatedMarketData(Mockito.any());
    }

    @Test
    public void sendManyUpdatedTest() throws InterruptedException {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        new Thread(()->{
            mdp.startPublisher(3, 1000);
        }).start();
        MarketData md1 = new MarketData("APPL", 100, System.currentTimeMillis());
        mdp.onMessage(md1);
        Thread.sleep(100);
        Mockito.verify(mdp).publishAggregatedMarketData(md1);

        for (int i = 1; i <= 5; i++){
            mdp.onMessage(new MarketData("APPL", 100 + i, System.currentTimeMillis()));
            Thread.sleep(1000);
        }
        Thread.sleep(1000);

        Mockito.verify(mdp, Mockito.times(6)).publishAggregatedMarketData(Mockito.any());
    }
}