package com.axonivy.market;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MarketplaceServiceApplication {

  private ProductService productService;

  public MarketplaceServiceApplication(ProductService productService) {
    this.productService = productService;
  }

  public static void main(String[] args) {
    SpringApplication.run(MarketplaceServiceApplication.class, args);
  }

  @Async
  @EventListener(ApplicationStartedEvent.class)
  public void startInitializeSystem() {
    syncProductData();
  }

  private void syncProductData() {
    var watch = new StopWatch();
    log.warn("Synchronizing Market repo: Started synchronizing data for Axon Ivy Market repo");
    watch.start();
    if (productService.syncLatestDataFromMarketRepo()) {
      log.warn("Synchronizing Market repo: Data is already up to date");
    } else {
      watch.stop();
      log.warn("Synchronizing Market repo: Finished synchronizing data for Axon Ivy Market repo in [{}] milliseconds",
          watch.getTime());
    }
  }
}
