package com.axonivy.market.service;

import com.axonivy.market.model.GithubReposModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface GithubReposService {

  /**
   * <p>
   * Load and store test reports
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link }
   * @author ttan
   */
  void loadAndStoreTestReports() throws IOException;

  /**
   * <p>
   * Load and store test reports for one product by product id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @return {@link }
   * @author ttan
   */
  void loadAndStoreTestReportsForOneProduct(String productId) throws IOException;

  /**
   * <p>
   * Update focused repository by list of repos
   * </p>
   *
   * @param  repos
   *              type {@link List<String>}
   * @return {@link }
   * @author ttan
   */
  void updateFocusedRepo(List<String> repos);

  /**
   * <p>
   * Fetch all repositories by conditions
   * </p>
   *
   * @param  isFocused
   *              type {@link Boolean}
   * @param  searchText
   *              type {@link String}
   * @param  workFlowType
   *              type {@link String}
   * @param  sortDirection
   *              type {@link String}
   * @param  pageable
   *              type {@link Pageable}
   * @return {@link Page<GithubReposModel>}
   * @author tvtphuc
   */
  Page<GithubReposModel> fetchAllRepositories(Boolean isFocused, String searchText, String workFlowType,
      String sortDirection, Pageable pageable);

}
