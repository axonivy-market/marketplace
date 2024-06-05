package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final GHAxonIvyMarketRepoService githubService;
    private final GithubRepoMetaRepository repoMetaRepository;

    public ProductServiceImpl(ProductRepository repo, GHAxonIvyMarketRepoService githubService,
                              GithubRepoMetaRepository repoMetaRepository) {
        this.repo = repo;
        this.githubService = githubService;
        this.repoMetaRepository = repoMetaRepository;
    }

    /**
     * Find in DB first, if no call GH API TODO When we must refresh data
     *
     * @throws Exception
     **/
    @Override
    public Page<Product> fetchAll(String type, Pageable pageable) {
        boolean hasChanged = false;
        Page<Product> products = Page.empty();
        switch (FilterType.of(type)) {
            case ALL -> products = repo.findAll(pageable);
            case CONNECTORS, UTILITIES, SOLUTIONS -> {
                products = repo.findByType(type, pageable);
            }
            default -> products = Page.empty();
        }

        if (products.isEmpty() || !checkGithubLastCommit()) {
            products = findProductsFromGithubRepo();
            hasChanged = true;
        }
        if (hasChanged) {
            syncGHDataToDB(products.toList());
        }
        return products;
    }

    private boolean checkGithubLastCommit() {
        // TODO check last commit
        boolean isLastCommitCovered;
        long lastCommitTime = 0L;
        var lastCommit = githubService.getLastCommit();
        if (lastCommit != null) {
            try {
                lastCommitTime = lastCommit.getCommitDate().getTime();
            } catch (IOException e) {
                log.error("Check last commit failed", e);
            }
        }

        var repoMeta = repoMetaRepository.findByRepoName("market");
        if (repoMeta != null && repoMeta.getLastChange() == lastCommitTime) {
            isLastCommitCovered = true;
        } else {
            isLastCommitCovered = false;
            repoMeta = new GithubRepoMeta();
            repoMeta.setRepoName("market");
            repoMeta.setLastChange(lastCommitTime);
            repoMetaRepository.save(repoMeta);
        }
        return isLastCommitCovered;
    }

    public Page<Product> findProductsFromGithubRepo() {
        var githubContentMap = githubService.fetchAllMarketItems();
        Page<Product> products = Page.empty();
        for (var contentKey : githubContentMap.keySet()) {
            Product product = new Product();
            for (var content : githubContentMap.get(contentKey)) {
                ProductFactory.mappingByGHContent(product, content);
            }
            products.and(product);
        }
        return products;
    }

    private void syncGHDataToDB(List<Product> products) {
        List<Product> modifiedProducts = new ArrayList<>();
        List<Product> deletedProducts = new ArrayList<>();
        var existingData = repo.findAll();
        for (var product : existingData) {
            var modifiedProduct = products.stream().filter(ghProduct -> product.getKey().equals(ghProduct.getKey())).findAny()
                    .orElse(null);
            if (modifiedProduct == null) {
                deletedProducts.add(product);
            } else {
                modifiedProducts.add(modifiedProduct);
            }
        }
        var newProducts = products.stream().filter(ghProduct -> !modifiedProducts.contains(ghProduct))
                .collect(Collectors.toList());
        // Update existing products
        if (!CollectionUtils.isEmpty(modifiedProducts)) {
            repo.saveAll(modifiedProducts);
        }
        // Insert new products
        if (!CollectionUtils.isEmpty(newProducts)) {
            repo.saveAll(newProducts);
        }
        // Delete obsoleted products
        if (!CollectionUtils.isEmpty(deletedProducts)) {
            repo.deleteAll(deletedProducts);
        }
    }

    @Override
    public Product findByKey(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Product> fetchAll(String type, String sort, int page, int pageSize) {
        // TODO Auto-generated method stub
        return null;
    }
}
