package com.axonivy.market.schedulingtask;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.MavenDependencyService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Log4j2
@Component
@AllArgsConstructor
public class ScheduledTasks {

  private static final String SCHEDULING_TASK_PRODUCTS_CRON = "0 0 0/1 ? * *";
  // External documentation sync will start at 00:40 in order to prevent running at the same time with other
  private static final String SCHEDULING_TASK_DOCUMENTS_CRON = "0 40 0 * * *";

  final ProductRepository productRepo;
  final ProductService productService;
  final ProductDetailsController productDetailsController;
  final ExternalDocumentService externalDocumentService;
  final MavenDependencyService mavenDependencyService;

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    log.warn("Started sync data for product from GitHub repo");
    productService.syncLatestDataFromMarketRepo(false);
  }

  @Scheduled(cron = SCHEDULING_TASK_DOCUMENTS_CRON)
  public void syncDataForProductDocuments() {
    log.warn("Started sync data for product document");
    for (var product : productRepo.findAllProductsHaveDocument()) {
      externalDocumentService.syncDocumentForProduct(product.getId(), new ArrayList<>(), false);
    }
  }

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductMavenDependencies() {
    log.warn("Started sync data for product maven dependencies");
    mavenDependencyService.syncIARDependenciesForProducts(false);
  }

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductReleases() throws IOException {
    log.warn("Started sync data for product releases");
    productDetailsController.syncLatestReleasesForProducts();
  }
}
