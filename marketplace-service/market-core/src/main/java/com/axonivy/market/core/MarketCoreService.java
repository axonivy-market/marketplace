package com.axonivy.market.core;

import org.springframework.stereotype.Component;

@Component
public class MarketCoreService {
    public String getCoreMessage() {
        return "Hello from market-core!";
    }
}
