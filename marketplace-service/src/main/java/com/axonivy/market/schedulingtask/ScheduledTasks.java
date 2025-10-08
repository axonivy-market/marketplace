package com.axonivy.market.schedulingtask;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.GithubReposService;
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

  private static final String SCHEDULING_TASK_PRODUCTS_CRON = "${market.scheduling.products-cron}";
  // External documentation sync will start at 00:40 in order to prevent running at the same time with other
  private static final String SCHEDULING_TASK_DOCUMENTS_CRON = "${market.scheduling.documents-cron}";
  private static final String SCHEDULING_TASK_PRODUCT_RELEASE_NOTES_CRON = "${market.scheduling" +
      ".products-release-notes-cron}";
  private static final String SCHEDULING_TASK_GITHUB_REPOS = "${market.scheduling.github-repos-cron}";

  private final ProductRepository productRepo;
  private final ProductService productService;
  private final ProductDetailsController productDetailsController;
  private final ExternalDocumentService externalDocumentService;
  private final ProductDependencyService productDependencyService;
  private final GithubReposService githubReposService;

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

  @Scheduled(cron = SCHEDULING_TASK_GITHUB_REPOS)
  public void syncDataForGithubRepos() throws IOException {
    log.warn("Started sync data for Github repositories");
    githubReposService.loadAndStoreTestReports();
  }
}
