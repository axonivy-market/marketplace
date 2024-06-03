package com.axonivy.market.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.enums.FilterType;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.model.Product;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository repo;
  private final GHAxonIvyMarketRepoService githubService;

  public ProductServiceImpl(ProductRepository repo, GHAxonIvyMarketRepoService githubService) {
    this.repo = repo;
    this.githubService = githubService;
  }

  /**
    Find in DB first, if no call GH API
    TODO When we must refresh data
  **/
  @Override
  public List<Product> fetchAll(String type, String sort, int page, int pageSize) {
    List<Product> products = new ArrayList<Product>();
    switch (FilterType.of(type)) {
    case ALL -> products.addAll(repo.findAll());
    case CONNECTORS, UTILITIES, SOLUTIONS -> {
      products.addAll(repo.findByType(type));
    }
    default -> throw new IllegalArgumentException("Unexpected value: " + type);
    }

    //TODO check last commit
    githubService.getLastCommit();
    
    if (CollectionUtils.isEmpty(products)) {
      // Find on GH
      products = findProductsFromGithubRepo();
      // Sync to DB
      // syncGHDataToDB(products);
    }
    return products;
  }

  private List<Product> findProductsFromGithubRepo() {
    var githubContentMap = githubService.fetchAllMarketItems();
    List<Product> products = new ArrayList<Product>();
    for (var contentKey : githubContentMap.keySet()) {
      Product product = new Product();
      for (var content : githubContentMap.get(contentKey)) {
        ProductFactory.mappingByGHContent(product, content);
      }
      products.add(product);
    }
    return List.of();
  }

  private void syncGHDataToDB(List<Product> products) {
    // TODO Store to MongoDB
    repo.saveAll(products);
  }

  @Override
  public Product findByKey(String key) {
    // TODO Auto-generated method stub
    return null;
  }

}
