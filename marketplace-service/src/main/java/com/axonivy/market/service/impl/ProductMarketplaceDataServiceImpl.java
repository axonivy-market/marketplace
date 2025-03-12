package com.axonivy.market.service.impl;

import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class ProductMarketplaceDataServiceImpl implements ProductMarketplaceDataService {
  private final ProductMarketplaceDataRepository productMarketplaceDataRepo;
  private final ProductCustomSortRepository productCustomSortRepo;
  private final ProductRepository productRepo;
  private final FileDownloadService fileDownloadService;
  private final ObjectMapper mapper = new ObjectMapper();
  private final SecureRandom random = new SecureRandom();
  @Value("${market.legacy.installation.counts.path}")
  private String legacyInstallationCountPath;

  public ProductMarketplaceDataServiceImpl(ProductMarketplaceDataRepository productMarketplaceDataRepo,
      ProductCustomSortRepository productCustomSortRepo, ProductRepository productRepo,
      FileDownloadService fileDownloadService) {
    this.productMarketplaceDataRepo = productMarketplaceDataRepo;
    this.productCustomSortRepo = productCustomSortRepo;
    this.productRepo = productRepo;
    this.fileDownloadService = fileDownloadService;
  }

  @Override
  public void addCustomSortProduct(ProductCustomSortRequest customSort) {
    SortOption.of(customSort.getRuleForRemainder());

    ProductCustomSort productCustomSort = new ProductCustomSort(customSort.getRuleForRemainder());
    productCustomSortRepo.deleteAll();
    productMarketplaceDataRepo.resetCustomOrderForAllProducts();
    productCustomSortRepo.save(productCustomSort);
    productMarketplaceDataRepo.saveAll(refineOrderedListOfProductsInCustomSort(customSort.getOrderedListOfProducts()));
  }

  public List<ProductMarketplaceData> refineOrderedListOfProductsInCustomSort(List<String> orderedListOfProducts) {
    List<ProductMarketplaceData> productEntries = new ArrayList<>();

    int descendingOrder = orderedListOfProducts.size();
    for (String productId : orderedListOfProducts) {
      validateProductExists(productId);
      ProductMarketplaceData productMarketplaceData = getProductMarketplaceData(productId);

      productMarketplaceData.setCustomOrder(descendingOrder--);
      productEntries.add(productMarketplaceData);
    }
    return productEntries;
  }

  @Override
  public ByteArrayResource downloadArtifact(String artifactUrl, String productId) {
    try {
      byte[] fileData = fileDownloadService.downloadFile(artifactUrl);

      if (fileData == null || fileData.length == 0) {
        return null;
      }

      updateInstallationCountForProduct(productId, null);

      return new ByteArrayResource(fileData);
    } catch (Exception e) {
      log.error("Error downloading file from URL {}: {}", artifactUrl, e.getMessage());
      return null;
    }
  }

  @Override
  public int updateInstallationCountForProduct(String productId, String designerVersion) {
    validateProductExists(productId);
    ProductMarketplaceData productMarketplaceData = getProductMarketplaceData(productId);

    log.info("Increase installation count for product {} By Designer Version {}", productId, designerVersion);
    if (StringUtils.isNotBlank(designerVersion)) {
      productMarketplaceDataRepo.increaseInstallationCountForProductByDesignerVersion(productId, designerVersion);
    }

    log.info("updating installation count for product {}", productId);
    if (BooleanUtils.isTrue(productMarketplaceData.getSynchronizedInstallationCount())) {
      return productMarketplaceDataRepo.increaseInstallationCount(productId);
    }
    int installationCount = getInstallationCountFromFileOrInitializeRandomly(productId);
    return productMarketplaceDataRepo.updateInitialCount(productId, installationCount + 1);
  }

  public int getInstallationCountFromFileOrInitializeRandomly(String productId) {
    log.info("synchronizing installation count for product {}", productId);
    int result = 0;
    try {
      String installationCounts = Files.readString(Paths.get(legacyInstallationCountPath));
      Map<String, Integer> mapping = mapper.readValue(installationCounts,
          new TypeReference<HashMap<String, Integer>>() {
          });
      List<String> keyList = mapping.keySet().stream().toList();
      result = keyList.contains(productId) ? mapping.get(productId) : random.nextInt(20, 50);
      log.info("synchronized installation count for product {} successfully", productId);
    } catch (IOException ex) {
      log.error("Could not read the marketplace-installation file to synchronize", ex);
    }
    return result;
  }

  @Override
  public int updateProductInstallationCount(String id) {
    ProductMarketplaceData productMarketplaceData = getProductMarketplaceData(id);
    if (BooleanUtils.isNotTrue(productMarketplaceData.getSynchronizedInstallationCount())) {
      return productMarketplaceDataRepo.updateInitialCount(id,
          getInstallationCountFromFileOrInitializeRandomly(id));
    }
    return productMarketplaceData.getInstallationCount();
  }

  @Override
  public ProductMarketplaceData getProductMarketplaceData(String productId) {
    return productMarketplaceDataRepo.findById(productId).orElse(
        ProductMarketplaceData.builder().id(productId).build());
  }

  private void validateProductExists(String productId) throws NotFoundException {
    if (productRepo.findById(productId).isEmpty()) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
    }
  }
}
