package com.axonivy.market.schedulingtask;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.factory.DisabledSecurityEventFactory;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.ProductSecurityInfoRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.NotificationService;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Log4j2
@Component
@AllArgsConstructor
public class ScheduledTasks {

  private final ProductRepository productRepo;
  private final ProductService productService;
  private final ProductDetailsController productDetailsController;
  private final ExternalDocumentService externalDocumentService;
  private final ProductDependencyService productDependencyService;
  private final GithubReposService githubReposService;
  private final GitHubService gitHubService;
  private final NotificationService notificationService;
  private final ProductSecurityInfoRepository productSecurityInfoRepository;

  public void syncDataForProductFromGitHubRepo() {
    productService.syncLatestDataFromMarketRepo(false);
  }

  public void syncDataForProductDocuments() {
    for (var product : productRepo.findAllProductsHaveDocument()) {
      externalDocumentService.syncDocumentForProduct(product.getId(), false, null);
    }
  }

  public void syncDataForProductMavenDependencies() {
    productDependencyService.syncIARDependenciesForProducts(false, null);

  }

  public void syncDataForProductReleases() {
    try {
      productDetailsController.syncLatestReleasesForProducts();
    } catch (IOException e) {
      log.error("Failed to sync data for product release notes: ", e);
    }
  }

  public void syncDataForGithubRepos() {
    try {
      githubReposService.loadAndStoreTestReports();
    } catch (IOException e) {
      log.warn("Sync data failed", e);
    }
  }

  public void sendNotificationForSecurityMonitor() {
    try {
      sendNotificationForDisabledSecurityChecks();
    } catch (IOException e) {
      log.warn("Sync security monitor failed", e);
    }
  }

  public void syncSecurityMonitor() {
    {
      try {
        gitHubService.syncSecurityDetailsForProduct();
      } catch (IOException e) {
        log.warn("Sync security monitor failed", e);
      }
    }
  }

  private static void run(Runnable runnable, String schedulingTaskName) {
    String threadName = Thread.currentThread().getName();
    var stopWatch = new StopWatch();
    stopWatch.start();
    log.warn("Thread {}: Started sync data for the '{}'", threadName, schedulingTaskName);
    runnable.run();
    stopWatch.stop();
    log.warn("Thread {}: Ended sync data for the '{}' in [{}] millisecond(s)", threadName, schedulingTaskName,
        stopWatch.getTime());
  }

  private void sendNotificationForDisabledSecurityChecks() throws IOException {
    List<DisabledSecurityEvent> disabledEvents = productSecurityInfoRepository.findAll().stream().flatMap(
        info -> DisabledSecurityEventFactory.from(info).stream()).toList();

    notificationService.notify(disabledEvents);
  }
}