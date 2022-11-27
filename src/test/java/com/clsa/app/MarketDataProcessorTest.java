package com.clsa.app;

import com.clsa.app.model.MarketData;
import org.junit.Test;
import org.mockito.Mockito;

public class MarketDataProcessorTest {
    @Test
    public void sendOneSymbolTest() {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        mdp.onMessage(new MarketData("APPL", 100, System.currentTimeMillis()));
        mdp.onMessage(new MarketData("APPL", 110, System.currentTimeMillis()));
        MarketData md3 = new MarketData("APPL", 120, System.currentTimeMillis());
        mdp.onMessage(md3);
        new Thread(()->{
            mdp.startPublisher(3, 1000);
        }).start();
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
        Mockito.verify(mdp).publishAggregatedMarketData(md1);

        MarketData md2 = new MarketData("APPL", 110, System.currentTimeMillis());
        mdp.onMessage(md2);
        // check that method wasn't called, cause we have to send one symbol once during sliding window
        Mockito.verify(mdp, Mockito.times(0)).publishAggregatedMarketData(md2);
        Thread.sleep(1000);
        // after sliding window is over, we should call it again
        Mockito.verify(mdp).publishAggregatedMarketData(md2);
    }
}
