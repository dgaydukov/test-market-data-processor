package com.clsa.app;

import com.clsa.app.model.MarketData;
import com.clsa.app.model.PublishedMarketData;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * use queue to store elements
 * use map as conflator to always get latest update
 */
public class MarketDataProcessor {
    private final Map<String, PublishedMarketData> map = new ConcurrentHashMap<>();
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    /**
     * @param throttleValue    - number of throttle messages during throttleInterval
     * @param throttleInterval - interval in ms, so if we want 100 message per second => startPublisher(100, 1000)
     */
    public void startPublisher(int throttleValue, int throttleInterval) {
        try {
            Set<String> sentSymbols = new HashSet<>();
            int throttle = 0;
            long start = System.currentTimeMillis();
            while (true) {
                // calculate sliding window
                if (throttle >= throttleValue || System.currentTimeMillis() - start > throttleInterval) {
                    while (System.currentTimeMillis() - start < throttleInterval) {
                        Thread.sleep(10);
                    }
                    throttle = 0;
                    start = System.currentTimeMillis();
                    sentSymbols.clear();
                }

                final String symbol = queue.take();
                final PublishedMarketData data = map.get(symbol);

                // we already published latest update for this symbol
                if (data.isPublished()) {
                    continue;
                }
                // if we publish symbol during current sliding window, we still need to send updated value during nex sliding window
                // so we add such symbol back to queue, to process it somewhere in next sliding window
                if (sentSymbols.contains(symbol)) {
                    queue.add(symbol);
                    continue;
                }

                data.setPublished(true);
                sentSymbols.add(symbol);
                publishAggregatedMarketData(data.getMarketData());
                throttle++;

            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Receive incoming market data
    public void onMessage(MarketData data) {
        map.compute(data.getSymbol(), (k, v) -> {
            if (v == null) {
                return new PublishedMarketData(data);
            }
            if (v.getMarketData().getUpdateTime() <= data.getUpdateTime()) {
                v.setMarketData(data);
                v.setPublished(false);
            }
            return v;
        });
        queue.add(data.getSymbol());
    }

    // Publish aggregated and throttled market data
    public void publishAggregatedMarketData(MarketData data) {
        System.out.println("publishAggregatedMarketData => " + data);
        // Do Nothing, assume implemented.
    }
}