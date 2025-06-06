package com.axonivy.market;

import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Log4j2
@EnableAsync
@EnableScheduling
@SpringBootApplication
@AllArgsConstructor
@EnableCaching
@EnableJpaAuditing
public class MarketplaceServiceApplication {

  final ProductService productService;
  final ExternalDocumentService externalDocumentService;

  public static void main(String[] args) {
    SpringApplication.run(MarketplaceServiceApplication.class, args);
  }

  @Async
  @EventListener(ApplicationStartedEvent.class)
  public void startInitializeSystem() {
    List<String> productIds = syncProductData();
    syncExternalDocumentData(productIds);
  }

  private List<String> syncProductData() {
    var watch = new StopWatch();
    log.warn("Synchronizing Market repo: Started synchronizing data for Axon Ivy Market repo");
    watch.start();
    List<String> syncedProductIds = productService.syncLatestDataFromMarketRepo(false);
    if (ObjectUtils.isEmpty(syncedProductIds)) {
      log.warn("Synchronizing Market repo: Nothing updated");
    } else {
      watch.stop();
      log.warn("Synchronizing Market repo: Finished synchronizing data for Axon Ivy Market repo in [{}] milliseconds",
          watch.getTime());
      log.warn("Synchronizing Market repo: Synced products [{}]", syncedProductIds);
    }
    return syncedProductIds;
  }

  private void syncExternalDocumentData(List<String> productIds) {
    var watch = new StopWatch();
    log.warn("Synchronizing External Document: Started synchronizing data for Document");
    watch.start();
    if (ObjectUtils.isEmpty(productIds)) {
      log.warn("Synchronizing External Document: Nothing updated");
    }
    productIds.forEach(id -> externalDocumentService.syncDocumentForProduct(id, false, null));
    watch.stop();
    log.warn("Synchronizing External Document: Finished synchronizing data for Document in [{}] milliseconds",
        watch.getTime());
  }
}
