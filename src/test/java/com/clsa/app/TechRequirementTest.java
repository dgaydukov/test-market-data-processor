package com.clsa.app;

import com.clsa.app.model.MarketData;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TechRequirementTest {
    /**
     * Ensure that the number of calls of publishAggregatedMarketData method
     * for publishing messages does not exceed 100 times per second, where this
     * period is a sliding window.
     */
    @Test
    public void ensureThrottlingTest() throws InterruptedException {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        new Thread(() -> {
            mdp.startPublisher(3, 1000);
        }).start();
        for (int i = 0; i < 10; i++) {
            mdp.onMessage(new MarketData("APPL" + i, 100, System.currentTimeMillis()));
        }
        Thread.sleep(100);
        Mockito.verify(mdp, Mockito.times(3)).publishAggregatedMarketData(Mockito.any());
        Thread.sleep(1000);
        Mockito.verify(mdp, Mockito.times(6)).publishAggregatedMarketData(Mockito.any());
        Thread.sleep(1000);
        Mockito.verify(mdp, Mockito.times(9)).publishAggregatedMarketData(Mockito.any());
    }

    /**
     * Ensure that each symbol does not update more than once per sliding
     * window.
     */
    @Test
    public void ensureUniqueSymbolPerSlidingWindow() throws InterruptedException {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        new Thread(() -> {
            mdp.startPublisher(3, 1000);
        }).start();
        for (int i = 1; i <= 10; i++) {
            mdp.onMessage(new MarketData("APPL", 100 + i, System.currentTimeMillis()));
        }
        Thread.sleep(100);
        for (int i = 11; i <= 20; i++) {
            mdp.onMessage(new MarketData("APPL", 100 + i, System.currentTimeMillis()));
        }
        // even as we added 20 updates for single ticket, publish called exactly once
        Mockito.verify(mdp, Mockito.times(1)).publishAggregatedMarketData(Mockito.any());
        Thread.sleep(200);
        // since we still in same sliding window, no new calls here for this ticket
        Mockito.verify(mdp, Mockito.times(1)).publishAggregatedMarketData(Mockito.any());
        Thread.sleep(1000);
        // now we in another sliding window, so we can publish this ticket update
        Mockito.verify(mdp, Mockito.times(2)).publishAggregatedMarketData(Mockito.any());
    }

    /**
     * Ensure that each symbol always has the latest market data published.
     */
    @Test
    public void ensureLatestPriceWindow() {
        MarketDataProcessor mdp = Mockito.spy(new MarketDataProcessor());
        for (int i = 1; i <= 30; i++) {
            mdp.onMessage(new MarketData("APPL", 100 + i, System.currentTimeMillis()));
        }
        new Thread(() -> {
            mdp.startPublisher(3, 1000);
        }).start();
        ArgumentCaptor<MarketData> argument = ArgumentCaptor.forClass(MarketData.class);
        Mockito.verify(mdp, Mockito.times(1)).publishAggregatedMarketData(argument.capture());
        Assert.assertEquals("values not matching", 130, argument.getValue().getPrice());
    }
}