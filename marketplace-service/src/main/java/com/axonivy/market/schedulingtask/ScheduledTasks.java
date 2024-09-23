package com.axonivy.market.schedulingtask;

import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductDocumentService;
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
  private static final String SCHEDULING_TASK_DOCUMENTS_CRON = "0 0 0 ? * *";

  final ProductRepository productRepo;
  final ProductService productService;
  final ProductDocumentService productDocumentService;

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    log.warn("Started sync data for product from GitHub repo");
    productService.syncLatestDataFromMarketRepo();
  }

  @Scheduled(cron = SCHEDULING_TASK_DOCUMENTS_CRON)
  public void syncDataForProductDocuments() {
    log.warn("Started sync data for product document");
    for (var product : productRepo.findAllProductsHaveDocument()) {
      productDocumentService.syncDocumentForProduct(product.getId(), false);
    }
  }
}
