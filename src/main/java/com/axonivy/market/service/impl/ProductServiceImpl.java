package com.axonivy.market.service.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.ReadmeModel;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GithubService;
import com.axonivy.market.github.util.GithubUtils;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final GHAxonIvyMarketRepoService axonivyMarketRepoService;
    private final GHAxonIvyProductRepoService axonivyProductRepoService;
    private final GithubRepoMetaRepository githubRepoMetaRepository;
    private final GithubService githubService;
    private GHCommit lastGHCommit;
    private GithubRepoMeta marketRepoMeta;
    public static final String NON_NUMERIC_CHAR = "[^0-9.]";

    public ProductServiceImpl(ProductRepository productRepository, GHAxonIvyMarketRepoService axonivyMarketRepoService,
                              GHAxonIvyProductRepoService axonivyProductRepoService, GithubRepoMetaRepository githubRepoMetaRepository, GithubService githubService) {
        this.productRepository = productRepository;
        this.axonivyMarketRepoService = axonivyMarketRepoService;
        this.axonivyProductRepoService = axonivyProductRepoService;
        this.githubRepoMetaRepository = githubRepoMetaRepository;
        this.githubService = githubService;
    }

    @Override
    public Page<Product> findProducts(String type, String keyword, Pageable pageable) {
        final var filterType = FilterType.of(type);
        if (StringUtils.isNoneBlank(keyword)) {
            return searchProducts(filterType, keyword, pageable);
        }

        if (!isLastGithubCommitCovered()) {
            if (marketRepoMeta == null) {
                syncProductsFromGithubRepo();
                marketRepoMeta = new GithubRepoMeta();
            } else {
                updateLatestChangeToProductsFromGithubRepo();
            }
            syncRepoMetaDataStatus();
        }

        Pageable unifiedPageabe = refinePagination(pageable);
        return switch (filterType) {
            case ALL -> productRepository.findAll(unifiedPageabe);
            case CONNECTORS, UTILITIES, SOLUTIONS -> productRepository.findByType(filterType.getCode(), pageable);
            default -> Page.empty();
        };
    }

    private void syncRepoMetaDataStatus() {
        if (marketRepoMeta == null || lastGHCommit == null) {
            return;
        }
        String repoURL = Optional.ofNullable(lastGHCommit.getOwner()).map(GHRepository::getUrl).map(URL::getPath)
                .orElse(EMPTY);
        marketRepoMeta.setRepoURL(repoURL);
        marketRepoMeta.setRepoName(GitHubConstants.MARKETPLACE_REPO_NAME);
        marketRepoMeta.setLastSHA1(lastGHCommit.getSHA1());
        marketRepoMeta.setLastChange(GithubUtils.getGHCommitDate(lastGHCommit));
        githubRepoMetaRepository.save(marketRepoMeta);
        marketRepoMeta = null;
    }

    private void updateLatestChangeToProductsFromGithubRepo() {
        if (lastGHCommit == null || marketRepoMeta == null) {
            return;
        }
        List<GitHubFile> githubFileChanges = marketRepoService
                .fetchMarketItemsBySHA1Range(marketRepoMeta.getLastSHA1(), lastGHCommit.getSHA1());
        Map<String, List<GitHubFile>> groupGithubFiles = new HashMap<>();
        for (var file : githubFileChanges) {
            String filePath = file.getFileName();
            var parentPath = filePath.replace(FileType.META.getFileName(), EMPTY).replace(FileType.LOGO.getFileName(), EMPTY);
            var files = groupGithubFiles.getOrDefault(parentPath, new ArrayList<>());
            files.add(file);
            groupGithubFiles.putIfAbsent(parentPath, files);
        }

        groupGithubFiles.entrySet().forEach(ghFileEntity -> {
            for (var file : ghFileEntity.getValue()) {
                Product product = new Product();
                GHContent fileContent;
                try {
                    fileContent = githubService.getGHContent(marketRepoService.getRepository(), file.getFileName());
                } catch (IOException e) {
                    log.error("Get GHContent failed: ", e);
                    continue;
                }

                ProductFactory.mappingByGHContent(product, fileContent);
                updateLatestReleaseDateForProduct(product);
                if (FileType.META == file.getType()) {
                    modifyProductByMetaContent(file, product);
                } else {
                    modifyProductLogo(ghFileEntity.getKey(), file, product, fileContent);
                }
            }
        });
    }

    private void modifyProductLogo(String parentPath, GitHubFile file, Product product, GHContent fileContent) {
        Product result = null;
        switch (file.getStatus()) {
            case MODIFIED, ADDED:
                result = productRepository.findByMarketDirectoryRegex(parentPath);
                if (result != null) {
                    result.setLogoUrl(GithubUtils.getDownloadUrl(fileContent));
                    productRepository.save(result);
                }
                break;
            case REMOVED:
                result = productRepository.findByLogoUrl(product.getLogoUrl());
                if (result != null) {
                    productRepository.deleteById(result.getId());
                }
                break;
            default:
                break;
        }
    }

    private void modifyProductByMetaContent(GitHubFile file, Product product) {
        switch (file.getStatus()) {
            case MODIFIED, ADDED:
                productRepository.save(product);
                break;
            case REMOVED:
                productRepository.deleteById(product.getId());
                break;
            default:
                break;
        }
    }

    private Pageable refinePagination(Pageable pageable) {
        PageRequest pageRequest = (PageRequest) pageable;
        if (pageable != null && pageable.getSort() != null) {
            List<Order> orders = new ArrayList<>();
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
        marketRepoMeta = githubRepoMetaRepository.findByRepoName(GitHubConstants.MARKETPLACE_REPO_NAME);
        if (marketRepoMeta != null) {
            lastCommitTime = marketRepoMeta.getLastChange();
        }
        lastGHCommit = marketRepoService.getLastCommit(lastCommitTime);
        if (lastGHCommit != null && marketRepoMeta != null
                && StringUtils.equals(lastGHCommit.getSHA1(), marketRepoMeta.getLastSHA1())) {
            isLastCommitCovered = true;
        }
        return isLastCommitCovered;
    }

    private Page<Product> syncProductsFromGithubRepo() {
        var githubContentMap = marketRepoService.fetchAllMarketItems();
        List<Product> products = new ArrayList<>();
        githubContentMap.entrySet().forEach(ghContentEntity -> {
            Product product = new Product();
            for (var content : ghContentEntity.getValue()) {
                ProductFactory.mappingByGHContent(product, content);
                updateLatestReleaseDateForProduct(product);
                extractCompatibilityFromOldestTag(product);
            }
            products.add(product);
        });
        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }
        return new PageImpl<>(products);
    }

    private void updateLatestReleaseDateForProduct(Product product) {
        if (StringUtils.isBlank(product.getRepositoryName())) {
            return;
        }
        try {
            GHRepository productRepo = githubService.getRepository(product.getRepositoryName());
            GHTag lastTag = CollectionUtils.firstElement(productRepo.listTags().toList());
            product.setNewestPublishDate(lastTag.getCommit().getCommitDate());
            product.setNewestReleaseVersion(lastTag.getName());
        } catch (Exception e) {
            log.error("Cannot find repository by path {} {}", product.getRepositoryName(), e);
        }
    }

    public void extractCompatibilityFromOldestTag(Product product) {
        try {
            if (StringUtils.isBlank(product.getCompatibility())) {
                GHRepository productRepo = githubService.getRepository(product.getRepositoryName());
                GHTag oldestTag = CollectionUtils.lastElement(productRepo.listTags().toList());
                if (oldestTag != null) {
                    String compatibility = getCompatibilityFromNumericTag(oldestTag);
                    product.setCompatibility(compatibility);
                }
            }
        } catch (Exception e) {
            log.error("Cannot find repository by path {}", e);
        }
    }

    // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
    private String getCompatibilityFromNumericTag(GHTag oldestTag) {
        String numericTag = oldestTag.getName().replaceAll(NON_NUMERIC_CHAR, "");
        if (!numericTag.contains(".")) {
            return numericTag + ".0+";
        }
        int firstDot = numericTag.indexOf(".");
        int secondDot = numericTag.indexOf(".", firstDot + 1);
        if (secondDot == -1) {
            return numericTag + "+";
        }
        return numericTag.substring(0, secondDot) + "+";
    }

    private Page<Product> searchProducts(FilterType filterType, String keyword, Pageable pageable) {
        Pageable unifiedPageabe = refinePagination(pageable);
        if (FilterType.ALL == filterType) {
            return productRepository.searchByNameOrShortDescriptionRegex(keyword, unifiedPageabe);
        }
        return productRepository.searchByKeywordAndType(keyword, filterType.getCode(), unifiedPageabe);
    }

    @Override
    public Product fetchProductDetail(String id, String type) {
        return productRepository.findByIdAndType(id, type);
    }

    @Override
    public ReadmeModel getReadmeAndProductContentsFromTag(String productId, String tag) {
        return axonivyProductRepoService.getReadmeAndProductContentsFromTag(productRepository.findById(productId).get().getRepositoryName(), tag);
    }
}