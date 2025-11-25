package com.axonivy.market.schedulingtask;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Log4j2
@Component
@AllArgsConstructor
public class ScheduledTasks {

  private static final String SYNC_PRODUCTS_CRON = "${market.scheduling.products-cron}";
  private static final String SYNC_DOCUMENTS_CRON = "${market.scheduling.documents-cron}";
  private static final String SYNC_PRODUCT_RELEASE_NOTES_CRON = "${market.scheduling.products-release-notes-cron}";
  private static final String SYNC_GITHUB_REPOS = "${market.scheduling.github-repos-cron}";

  private final ProductRepository productRepo;
  private final ProductService productService;
  private final ProductDetailsController productDetailsController;
  private final ExternalDocumentService externalDocumentService;
  private final ProductDependencyService productDependencyService;
  private final GithubReposService githubReposService;
  private final ScheduledTaskServiceImpl scheduledTaskService;
  private final Environment environment;

  @Scheduled(cron = SYNC_PRODUCTS_CRON)
  public void syncDataForProductFromGitHubRepo() {
    execute("syncDataForProductFromGitHubRepo", SYNC_PRODUCTS_CRON, () -> productService.syncLatestDataFromMarketRepo(false),
        "Product from GitHub repo");
  }

  @Scheduled(cron = SYNC_DOCUMENTS_CRON)
  public void syncDataForProductDocuments() {
    execute("syncDataForProductDocuments", SYNC_DOCUMENTS_CRON, () -> {
      for (var product : productRepo.findAllProductsHaveDocument()) {
        externalDocumentService.syncDocumentForProduct(product.getId(), false, null);
      }
    }, "Product document");
  }

  @Scheduled(cron = SYNC_PRODUCTS_CRON)
  public void syncDataForProductMavenDependencies() {
    execute("syncDataForProductMavenDependencies", SYNC_PRODUCTS_CRON,
        () -> productDependencyService.syncIARDependenciesForProducts(false, null),
        "Product maven dependencies");
  }

  @Scheduled(cron = SYNC_PRODUCT_RELEASE_NOTES_CRON)
  public void syncDataForProductReleases() {
    execute("syncDataForProductReleases", SYNC_PRODUCT_RELEASE_NOTES_CRON, () -> {
      try {
        productDetailsController.syncLatestReleasesForProducts();
      } catch (IOException e) {
        log.error("Failed to sync data for product release notes: ", e);
      }
    }, "Product release notes");
  }

  @Scheduled(cron = SYNC_GITHUB_REPOS)
  public void syncDataForGithubRepos() {
    execute("syncDataForGithubRepos", SYNC_GITHUB_REPOS, () -> {
      try {
        githubReposService.loadAndStoreTestReports();
      } catch (IOException e) {
        log.warn("Sync data failed", e);
      }
    }, "Github repositories");
  }

  private void execute(String methodName, String cronPlaceholder, Runnable runnable, String schedulingTaskName) {
    String cronExpression = resolveCron(cronPlaceholder);
    scheduledTaskService.beforeExecute(methodName, cronExpression);
    String threadName = Thread.currentThread().getName();
    var stopWatch = new StopWatch();
    stopWatch.start();
    log.warn("Thread {}: Started sync data for the '{}'", threadName, schedulingTaskName);
    try {
      runnable.run();
      scheduledTaskService.afterSuccess(methodName);
    } catch (Exception e) {
      scheduledTaskService.afterFailure(methodName, e);
      throw e;
    } finally {
      stopWatch.stop();
      log.warn("Thread {}: Ended sync data for the '{}' in [{}] millisecond(s)", threadName, schedulingTaskName,
          stopWatch.getTime());
    }
  }

  private String resolveCron(String placeholder) {
    if (placeholder == null) {
      return null;
    }
    if (placeholder.startsWith("${") && placeholder.endsWith("}")) {
      String key = placeholder.substring(2, placeholder.length() - 1);
      return environment.getProperty(key);
    }
    return placeholder;
  }
}
