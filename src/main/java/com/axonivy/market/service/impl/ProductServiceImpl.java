package com.axonivy.market.service.impl;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.entity.ReadmeProductContent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
	private final GHAxonIvyProductRepoService axonIvyProductRepoService;
	private final GitHubRepoMetaRepository gitHubRepoMetaRepository;
	private final GitHubService gitHubService;

	private GHCommit lastGHCommit;
	private GitHubRepoMeta marketRepoMeta;

	public static final String NON_NUMERIC_CHAR = "[^0-9.]";

	public ProductServiceImpl(ProductRepository productRepository, GHAxonIvyMarketRepoService axonIvyMarketRepoService,
			GHAxonIvyProductRepoService axonIvyProductRepoService, GitHubRepoMetaRepository gitHubRepoMetaRepository,
			GitHubService gitHubService) {
		this.productRepository = productRepository;
		this.axonIvyMarketRepoService = axonIvyMarketRepoService;
		this.axonIvyProductRepoService = axonIvyProductRepoService;
		this.gitHubRepoMetaRepository = gitHubRepoMetaRepository;
		this.gitHubService = gitHubService;
	}

	@Override
	public Page<Product> findProducts(String type, String keyword, Pageable pageable) {
		final var typeOption = TypeOption.of(type);
		final var searchPageable = refinePagination(pageable);
		Page<Product> result = Page.empty();
		switch (typeOption) {
		case ALL:
			if (StringUtils.isBlank(keyword)) {
				result = productRepository.findAll(searchPageable);
			} else {
				result = productRepository.searchByNameOrShortDescriptionRegex(keyword, searchPageable);
			}
			break;
		case CONNECTORS, UTILITIES, SOLUTIONS:
			if (StringUtils.isBlank(keyword)) {
				result = productRepository.findByType(typeOption.getCode(), searchPageable);
			} else {
				result = productRepository.searchByKeywordAndType(keyword, typeOption.getCode(), searchPageable);
			}
			break;
		default:
			break;
		}
		return result;
	}

	@Override
	public boolean syncLatestDataFromMarketRepo() {
		var isAlreadyUpToDate = isLastGithubCommitCovered();
		if (!isAlreadyUpToDate) {
			if (marketRepoMeta == null) {
				syncProductsFromGitHubRepo();
				marketRepoMeta = new GitHubRepoMeta();
			} else {
				updateLatestChangeToProductsFromGithubRepo();
			}
			syncRepoMetaDataStatus();
		}
		return isAlreadyUpToDate;
	}

	private void syncRepoMetaDataStatus() {
		if (lastGHCommit == null) {
			return;
		}
		String repoURL = Optional.ofNullable(lastGHCommit.getOwner()).map(GHRepository::getUrl).map(URL::getPath)
				.orElse(EMPTY);
		marketRepoMeta.setRepoURL(repoURL);
		marketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
		marketRepoMeta.setLastSHA1(lastGHCommit.getSHA1());
		marketRepoMeta.setLastChange(GitHubUtils.getGHCommitDate(lastGHCommit));
		gitHubRepoMetaRepository.save(marketRepoMeta);
		marketRepoMeta = null;
	}

	private void updateLatestChangeToProductsFromGithubRepo() {
		var fromSHA1 = marketRepoMeta.getLastSHA1();
		var toSHA1 = ofNullable(lastGHCommit).map(GHCommit::getSHA1).orElse("");
		log.warn("**ProductService: synchronize products from SHA1 {} to SHA1 {}", fromSHA1, toSHA1);
		List<GitHubFile> gitHubFileChanges = axonIvyMarketRepoService.fetchMarketItemsBySHA1Range(fromSHA1, toSHA1);
		Map<String, List<GitHubFile>> groupGitHubFiles = new HashMap<>();
		for (var file : gitHubFileChanges) {
			String filePath = file.getFileName();
			var parentPath = filePath.replace(FileType.META.getFileName(), EMPTY).replace(FileType.LOGO.getFileName(),
					EMPTY);
			var files = groupGitHubFiles.getOrDefault(parentPath, new ArrayList<>());
			files.add(file);
			groupGitHubFiles.putIfAbsent(parentPath, files);
		}

		groupGitHubFiles.entrySet().forEach(ghFileEntity -> {
			for (var file : ghFileEntity.getValue()) {
				Product product = new Product();
				GHContent fileContent;
				try {
					fileContent = gitHubService.getGHContent(axonIvyMarketRepoService.getRepository(),
							file.getFileName());
				} catch (IOException e) {
					log.error("Get GHContent failed: ", e);
					continue;
				}

				ProductFactory.mappingByGHContent(product, fileContent);
				updateProductFromReleaseTags(product);
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
				result.setLogoUrl(GitHubUtils.getDownloadUrl(fileContent));
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
				final var sortOption = SortOption.of(sort.getProperty());
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
		marketRepoMeta = gitHubRepoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
		if (marketRepoMeta != null) {
			lastCommitTime = marketRepoMeta.getLastChange();
		}
		lastGHCommit = axonIvyMarketRepoService.getLastCommit(lastCommitTime);
		if (lastGHCommit != null && marketRepoMeta != null
				&& StringUtils.equals(lastGHCommit.getSHA1(), marketRepoMeta.getLastSHA1())) {
			isLastCommitCovered = true;
		}
		return isLastCommitCovered;
	}

	private Page<Product> syncProductsFromGitHubRepo() {
		log.warn("**ProductService: synchronize products from scratch based on the Market repo");
		var gitHubContentMap = axonIvyMarketRepoService.fetchAllMarketItems();
		List<Product> products = new ArrayList<>();
		gitHubContentMap.entrySet().forEach(ghContentEntity -> {
			Product product = new Product();
			for (var content : ghContentEntity.getValue()) {
				ProductFactory.mappingByGHContent(product, content);
				updateProductFromReleaseTags(product);
			}
			products.add(product);

		});

		if (!products.isEmpty()) {
			productRepository.saveAll(products);
		}
		return new PageImpl<>(products);
	}

	private void updateProductFromReleaseTags(Product product) {
		if (StringUtils.isBlank(product.getRepositoryName())) {
			return;
		}
		try {
			GHRepository productRepo = gitHubService.getRepository(product.getRepositoryName());
			List<GHTag> tags = productRepo.listTags().toList();
			GHTag lastTag = CollectionUtils.firstElement(tags);
			GHTag oldestTag = CollectionUtils.lastElement(tags);
			if (lastTag != null) {
				product.setNewestPublishedDate(lastTag.getCommit().getCommitDate());
				product.setNewestReleaseVersion(lastTag.getName());
			}

			if (oldestTag != null && StringUtils.isBlank(product.getCompatibility())) {
				String compatibility = getCompatibilityFromNumericTag(oldestTag);
				product.setCompatibility(compatibility);
			}

			List<CompletableFuture<ReadmeProductContent>> completableFutures = new ArrayList<>();
			for (GHTag ghtag : tags) {
				completableFutures.add(CompletableFuture.supplyAsync(() -> axonIvyProductRepoService
						.getReadmeAndProductContentsFromTag(productRepo, ghtag.getName())));
			}
			completableFutures.forEach(CompletableFuture::join);
			List<ReadmeProductContent> readmeProductContents = completableFutures.stream().map(f -> {
				try {
					return f.get();
				} catch (InterruptedException | ExecutionException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Thread was interrupted", e);
				}
			}).toList();
			product.setReadmeProductContents(readmeProductContents);
		} catch (Exception e) {
			log.error("Cannot find repository by path {} {}", product.getRepositoryName(), e);
		}
	}

	// Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
	@Override
	public String getCompatibilityFromNumericTag(GHTag oldestTag) {
		String numericTag = oldestTag.getName().replaceAll(NON_NUMERIC_CHAR, Strings.EMPTY);
		if (!numericTag.contains(CommonConstants.DOT)) {
			return numericTag + ".0+";
		}
		int firstDot = numericTag.indexOf(CommonConstants.DOT);
		int secondDot = numericTag.indexOf(CommonConstants.DOT, firstDot + 1);
		if (secondDot == -1) {
			return numericTag.concat(CommonConstants.PLUS);
		}
		return numericTag.substring(0, secondDot).concat(CommonConstants.PLUS);
	}

	@Override
	public Product fetchProductDetail(String id, String type) {
		return productRepository.findByIdAndType(id, type);
	}
}