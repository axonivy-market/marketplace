package com.axonivy.market.neo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.axonivy.market")
public class MarketNeoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketNeoApplication.class, args);
    }
}
