package com.axonivy.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MarketplaceServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MarketplaceServiceApplication.class, args);
  }

}
