package com.axonivy.market.neo;

import org.springframework.stereotype.Component;
import com.axonivy.market.core.MarketCoreService;

@Component
public class MarketNeoService {
    private final MarketCoreService coreService;

    public MarketNeoService(MarketCoreService coreService) {
        this.coreService = coreService;
    }

    public String getNeoMessage() {
        return "Neo says: " + coreService.getCoreMessage();
    }
}
