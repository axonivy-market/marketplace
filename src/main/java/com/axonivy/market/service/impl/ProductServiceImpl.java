package com.axonivy.market.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.ProductDetailModel;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.util.GithubUtils;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final GHAxonIvyMarketRepoService githubMarketRepoService;
    private final GHAxonIvyProductRepoService ghAxonIvyProductRepoService;
    private final GithubRepoMetaRepository repoMetaRepository;
    private GHCommit lastGHCommit;
    private GithubRepoMeta marketRepoMeta;
    private ProductDetailModelAssembler detailModelAssembler;
    public ProductServiceImpl(ProductRepository repo, GHAxonIvyMarketRepoService githubService,
                              GHAxonIvyProductRepoService ghAxonIvyProductRepoService, GithubRepoMetaRepository repoMetaRepository, ProductDetailModelAssembler detailModelAssembler) {
        this.productRepo = repo;
        this.githubMarketRepoService = githubService;
        this.ghAxonIvyProductRepoService = ghAxonIvyProductRepoService;
        this.repoMetaRepository = repoMetaRepository;
        this.detailModelAssembler = detailModelAssembler;
    }

    @Override
    public Page<Product> findProductsByType(String type, Pageable pageable) {
        if (!isLastGithubCommitCovered()) {
            if (marketRepoMeta == null) {
                syncProductsFromGithubRepo();
                marketRepoMeta = new GithubRepoMeta();
            } else {
                updateLatestChangeToProductsFromGithubRepo();
            }
            marketRepoMeta.setRepoURL(lastGHCommit.getOwner().getUrl().getPath());
            marketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
            marketRepoMeta.setLastSHA1(lastGHCommit.getSHA1());
            marketRepoMeta.setLastChange(GithubUtils.getGHCommitDate(lastGHCommit));
            repoMetaRepository.save(marketRepoMeta);
        }

        final FilterType filterType = FilterType.of(type);
        Pageable unifiedPageabe = refinePagination(pageable);

        return switch (filterType) {
            case ALL -> productRepo.findAll(unifiedPageabe);
            case CONNECTORS, UTILITIES, SOLUTIONS -> productRepo.findByType(filterType.getCode(), pageable);
            default -> Page.empty();
        };
    }

    private void updateLatestChangeToProductsFromGithubRepo() {
        if (lastGHCommit == null || marketRepoMeta == null) {
            return;
        }
        var githubFileChanges = githubMarketRepoService.fetchMarketItemsBySHA1Range(marketRepoMeta.getLastSHA1(),
                lastGHCommit.getSHA1());
        Map<String, List<GitHubFile>> groupedGithubFiles = new HashMap<>();
        for (var file : githubFileChanges) {
            var filePath = file.getFileName();
            var parentPath = filePath.replace(FileType.META.getFileName(), "").replace(FileType.LOGO.getFileName(), "");
            var files = groupedGithubFiles.getOrDefault(parentPath, new ArrayList<>());
            files.add(file);
            groupedGithubFiles.putIfAbsent(parentPath, files);
        }

        for (var parentPath : groupedGithubFiles.keySet()) {
            var files = groupedGithubFiles.get(parentPath);
            for (var file : files) {
                Product product = new Product();
                var fileContent = githubMarketRepoService.getGHContent(file.getFileName());
                ProductFactory.mappingByGHContent(product, fileContent);
                if (FileType.META == file.getType()) {
                    modifyProductByMetaContent(file, product);
                } else {
                    modifyProductLogo(parentPath, file, product, fileContent);
                }
            }
        }
    }

    private void modifyProductLogo(String parentPath, GitHubFile file, Product product, GHContent fileContent) {
        Product result = null;
        switch (file.getStatus()) {
            case MODIFIED, ADDED:
                result = productRepo.findByMarketDirectoryRegex(parentPath);
                if (result != null) {
                    result.setLogoUrl(GithubUtils.getDownloadUrl(fileContent));
                    productRepo.save(result);
                }
            case REMOVED:
                result = productRepo.findByLogoUrl(product.getLogoUrl());
                if (result != null) {
                    productRepo.deleteById(result.getKey());
                }
            default:
                break;
        }
    }

    private void modifyProductByMetaContent(GitHubFile file, Product product) {
        switch (file.getStatus()) {
            case MODIFIED, ADDED:
                productRepo.save(product);
            case REMOVED:
                productRepo.deleteById(product.getKey());
            default:
                break;
        }
    }

    private Pageable refinePagination(Pageable pageable) {
        PageRequest pageRequest = (PageRequest) pageable;
        if (pageable != null && pageable.getSort() != null) {
            List<Order> orders = new ArrayList<Sort.Order>();
            for (var sort : pageable.getSort()) {
                final SortOption sortOption = SortOption.of(sort.getProperty());
                Order order = new Order(sort.getDirection(), sortOption.getCode());
                orders.add(order);
            }
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
        }
        return pageRequest;
    }

    private boolean isLastGithubCommitCovered() {
        boolean isLastCommitCovered = false;
        long lastCommitTime = 0l;
        marketRepoMeta = repoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
        if (marketRepoMeta != null) {
            lastCommitTime = marketRepoMeta.getLastChange();
        }
        lastGHCommit = githubMarketRepoService.getLastCommit(lastCommitTime);
        if (lastGHCommit != null && marketRepoMeta != null && lastGHCommit.getSHA1().equals(marketRepoMeta.getLastSHA1())) {
            isLastCommitCovered = true;
        }
        return isLastCommitCovered;
    }

    private Page<Product> syncProductsFromGithubRepo() {
        var githubContentMap = githubMarketRepoService.fetchAllMarketItems();
        List<Product> products = new ArrayList<>();
        for (var contentKey : githubContentMap.keySet()) {
            Product product = new Product();
            for (var content : githubContentMap.get(contentKey)) {
                ProductFactory.mappingByGHContent(product, content);
            }
            products.add(product);
        }
        productRepo.saveAll(products);
        return new PageImpl<Product>(products);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        Pageable unifiedPageabe = refinePagination(pageable);
        if (StringUtils.isBlank(keyword)) {
            return productRepo.findAll(pageable);
        }
        return productRepo.findByNameOrShortDescriptionRegex(keyword, unifiedPageabe);
    }


    public Product fetchProductDetail(String key) {
        return productRepo.findByKey(key);
    }

    public ProductDetailModel fetch(String key) {
        var productDetail = fetchProductDetail(key);
        ProductDetailModel productDetailModel = EntityModel.of(detailModelAssembler.toModel(productDetail)).getContent();
        GHContent productContent = ghAxonIvyProductRepoService.getContentFromGHRepoAndTag("adobe-acrobat-sign-connector", "adobe-esign-connector-product/README.md", "v10.0.15");
        try {
            productDetailModel.setContent(productContent.getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return productDetailModel;
    }
    }