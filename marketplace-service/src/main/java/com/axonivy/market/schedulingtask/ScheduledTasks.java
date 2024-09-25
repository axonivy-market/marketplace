package com.axonivy.market.schedulingtask;

import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ScheduledTasks {

  private static final String SCHEDULING_TASK_PRODUCTS_CRON = "0 0 0/1 ? * *";
  private static final String SCHEDULING_TASK_MAVEN_VERSION_CRON = "0 0 0 * * *";

  private final ProductService productService;
  private final MetadataService metadataService;

  public ScheduledTasks(ProductService productService, MetadataService metadataService) {
    this.productService = productService;
    this.metadataService = metadataService;
  }

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    log.warn("Started sync data for product from GitHub repo");
    productService.syncLatestDataFromMarketRepo();
  }

  @Scheduled(cron = SCHEDULING_TASK_MAVEN_VERSION_CRON)
  public void syncDataForMavenMetadata() {
    log.warn("Started sync data for Maven metadata");
    metadataService.syncAllProductMavenMetadata();
  }
}
