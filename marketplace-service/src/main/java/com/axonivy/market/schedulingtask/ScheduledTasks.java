package com.axonivy.market.schedulingtask;

import com.axonivy.market.service.MetadataService;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@AllArgsConstructor
public class ScheduledTasks {

  private static final String SCHEDULING_TASK_PRODUCTS_CRON = "0 0 0/1 ? * *";
  // Maven version sync will start at 00:20 in order to prevent running at the same time with product repo sync
  private static final String SCHEDULING_TASK_MAVEN_VERSION_CRON = "0 20 0 * * *";
  // External documentation sync will start at 00:40 in order to prevent running at the same time with other
  private static final String SCHEDULING_TASK_DOCUMENTS_CRON = "0 40 0 * * *";

  final ProductRepository productRepo;
  final ProductService productService;
  final ExternalDocumentService externalDocumentService;
  private final MetadataService metadataService;

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    log.warn("Started sync data for product from GitHub repo");
    productService.syncLatestDataFromMarketRepo();
  }

  @Scheduled(cron = SCHEDULING_TASK_MAVEN_VERSION_CRON)
  public void syncDataForMavenMetadata() {
    log.warn("Started sync data for Maven metadata");
    metadataService.syncAllProductsMetadata();
  }
  @Scheduled(cron = SCHEDULING_TASK_DOCUMENTS_CRON)
  public void syncDataForProductDocuments() {
    log.warn("Started sync data for product document");
    for (var product : productRepo.findAllProductsHaveDocument()) {
      externalDocumentService.syncDocumentForProduct(product.getId(), false);
    }
  }
}
