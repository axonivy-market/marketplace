package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.GithubReposService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubReposServiceImpl implements GithubReposService {

  private final GithubRepoRepository githubRepoRepository;
  private final ProductRepository productRepository;
  private final GithubReposModelAssembler githubReposModelAssembler;
  private final WorkflowServiceImpl workflowService;

  @Override
  public void loadAndStoreTestReports() {
    try {
      List<Product> products = productRepository.findAll();
      for (Product product : products) {
        workflowService.processProduct(product);
      }
    } catch (Exception e) {
      log.error("Error loading and storing test reports", e);
    }
  }

  @Override
  public List<GithubReposModel> fetchAllRepositories() {
    List<GithubRepo> entities = githubRepoRepository.findAll();
    return entities.stream()
        .map(githubReposModelAssembler::toModel)
        .collect(Collectors.toList());
  }
}