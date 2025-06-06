package com.axonivy.market.schedulingtask;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
@AllArgsConstructor
public class ScheduledTasks {

  private static final String SCHEDULING_TASK_PRODUCTS_CRON = "0 0 0/1 ? * *";
  // External documentation sync will start at 00:40 in order to prevent running at the same time with other
  private static final String SCHEDULING_TASK_DOCUMENTS_CRON = "0 40 0 * * *";
  private static final String SCHEDULING_TASK_PRODUCT_RELEASE_NOTES_CRON = "0 0 0 * * ?";

  final ProductRepository productRepo;
  final ProductService productService;
  final ProductDetailsController productDetailsController;
  final ExternalDocumentService externalDocumentService;
  final ProductDependencyService productDependencyService;

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    log.warn("Started sync data for product from GitHub repo");
    productService.syncLatestDataFromMarketRepo(false);
  }

  @Scheduled(cron = SCHEDULING_TASK_DOCUMENTS_CRON)
  public void syncDataForProductDocuments() {
    log.warn("Started sync data for product document");
    for (var product : productRepo.findAllProductsHaveDocument()) {
      externalDocumentService.syncDocumentForProduct(product.getId(), false, null);
    }
  }

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductMavenDependencies() {
    log.warn("Started sync data for product maven dependencies");
    productDependencyService.syncIARDependenciesForProducts(false, null);
  }

  @Scheduled(cron = SCHEDULING_TASK_PRODUCT_RELEASE_NOTES_CRON)
  public void syncDataForProductReleases() {
    log.warn("Started sync data for product release notes");
    try {
      productDetailsController.syncLatestReleasesForProducts();
    } catch (IOException e) {
      log.error("Failed to sync data for product release notes: ", e);
    }
  }
}
