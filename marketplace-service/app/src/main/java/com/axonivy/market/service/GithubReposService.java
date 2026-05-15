package com.axonivy.market.service;

import com.axonivy.market.model.GithubReposModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface GithubReposService {

  /**
   * <p>
   * Loads test reports from all GitHub repositories and stores them in the database. Queries each product's
   * GitHub repository for workflow execution results, builds status, and test reports, then persists this
   * information for monitoring and analytics.
   * </p>
   *
   * @return void - no return value; results are persisted directly to the repository storage
   * @throws IOException - if GitHub API communication fails
   * @author ttan
   */
  void loadAndStoreTestReports() throws IOException;

  /**
   * <p>
   * Loads test reports for a specific product from its GitHub repository and stores them in the database.
   * Queries the product's GitHub workflows, builds, and test executions, then persists the data for
   * later retrieval and analysis.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to load test reports for
   * @return void - no return value; results are persisted directly to the repository storage
   * @throws IOException - if GitHub API communication fails
   * @author ttan
   */
  void loadAndStoreTestReportsForOneProduct(String productId) throws IOException;

  /**
   * <p>
   * Updates the "focused" status of GitHub repositories. Marks specified repositories as focused/featured,
   * which affects their visibility and priority in the marketplace UI and search results.
   * </p>
   *
   * @param  repos
   *              type {@link List<String>} - list of repository names to mark as focused
   * @return void - no return value; updates are persisted directly to the repository storage
   * @author ttan
   */
  void updateFocusedRepo(List<String> repos);

  /**
   * <p>
   * Retrieves all GitHub repositories with advanced filtering and pagination. Supports filtering by focused
   * status, repository name/description search, workflow type, and sort direction. Results are paginated and
   * sorted according to the provided configuration.
   * </p>
   *
   * @param  isFocused
   *              type {@link Boolean} - if true, returns only focused/featured repositories; if false, returns
   *              non-focused repositories; null includes all repositories
   * @param  searchText
   *              type {@link String} - search keyword to filter by repository name or description; null for no filtering
   * @param  workFlowType
   *              type {@link String} - filter by GitHub workflow type (e.g., "build", "test"); null for all types
   * @param  sortDirection
   *              type {@link String} - sort order direction ("asc" for ascending, "desc" for descending)
   * @param  pageable
   *              type {@link Pageable} - pagination and sorting configuration
   * @return {@link Page<GithubReposModel>} - paginated list of repositories matching all filter criteria
   * @author tvtphuc
   */
  Page<GithubReposModel> fetchAllRepositories(Boolean isFocused, String searchText, String workFlowType,
      String sortDirection, Pageable pageable);

}
