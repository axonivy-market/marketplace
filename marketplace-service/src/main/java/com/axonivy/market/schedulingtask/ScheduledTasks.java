package com.axonivy.market.schedulingtask;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ScheduledTasks {

  private static final String SCHEDULING_TASK_PRODUCTS_CRON = "0 0 0/1 ? * *";

  private ProductService productService;

  public ScheduledTasks(ProductService productService) {
    this.productService = productService;
  }

  @Scheduled(cron = SCHEDULING_TASK_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    log.warn("Started sync data for product from GitHub repo");
    productService.syncLatestDataFromMarketRepo();
  }

}
