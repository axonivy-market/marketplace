package com.axonivy.market.service.impl;

import com.axonivy.market.core.entity.ProductCustomSort;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.enums.SortOption;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.service.impl.CoreProductMarketplaceDataServiceImpl;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Log4j2
@Service
@Primary
public class ProductMarketplaceDataServiceImpl extends CoreProductMarketplaceDataServiceImpl
    implements ProductMarketplaceDataService {
  private final ProductMarketplaceDataRepository productMarketplaceDataRepo;
  private final ProductCustomSortRepository productCustomSortRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final ProductRepository productRepo;
  private final FileDownloadService fileDownloadService;

  public ProductMarketplaceDataServiceImpl(
      ProductMarketplaceDataRepository productMarketplaceDataRepo, ProductCustomSortRepository productCustomSortRepo,
      MavenArtifactVersionRepository mavenArtifactVersionRepo, ProductRepository productRepo,
      ProductDesignerInstallationRepository productDesignerInstallationRepo, FileDownloadService fileDownloadService) {
    super(productMarketplaceDataRepo, productDesignerInstallationRepo, productRepo);
    this.productMarketplaceDataRepo = productMarketplaceDataRepo;
    this.productCustomSortRepo = productCustomSortRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.productRepo = productRepo;
    this.fileDownloadService = fileDownloadService;
  }

  @Override
  public void addCustomSortProduct(ProductCustomSortRequest customSort) {
    SortOption.of(customSort.getRuleForRemainder());

    var productCustomSort = new ProductCustomSort(customSort.getRuleForRemainder());
    productCustomSortRepo.deleteAll();
    productMarketplaceDataRepo.resetCustomOrderForAllProducts();
    productCustomSortRepo.save(productCustomSort);
    productMarketplaceDataRepo.saveAll(refineOrderedListOfProductsInCustomSort(customSort.getOrderedListOfProducts()));
  }

  public List<ProductMarketplaceData> refineOrderedListOfProductsInCustomSort(
      Collection<String> orderedListOfProducts) {
    List<ProductMarketplaceData> productEntries = new ArrayList<>();

    int descendingOrder = orderedListOfProducts.size();
    for (String productId : orderedListOfProducts) {
      validateProductExists(productId);
      var productMarketplaceData = getProductMarketplaceData(productId);

      int currentOrder = descendingOrder;
      descendingOrder--;
      productMarketplaceData.setCustomOrder(currentOrder);
      productEntries.add(productMarketplaceData);
    }
    return productEntries;
  }

  @Override
  public Integer getInstallationCount(String id) {
    return productMarketplaceDataRepo.findById(id)
        .map(ProductMarketplaceData::getInstallationCount)
        .orElse(0);
  }

  @Override
  public ResponseEntity<Resource> getProductArtifactStream(String productId, String artifactId, String version) {
    var mavenArtifactVersions = mavenArtifactVersionRepo.findByProductIdAndArtifactIdAndVersion(productId, artifactId,
        version);
    if (CollectionUtils.isEmpty(mavenArtifactVersions)) {
      return null;
    }
    String downloadUrl = mavenArtifactVersions.get(0).getDownloadUrl();
    return fileDownloadService.fetchUrlResource(downloadUrl);
  }

  private void validateProductExists(String productId) throws NotFoundException {
    if (productRepo.findById(productId).isEmpty()) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
    }
  }

  @Override
  public OutputStream buildArtifactStreamFromResource(String productId, Resource resource, OutputStream outputStream) {
    try (var inputStream = resource.getInputStream()) {
      FileUtils.writeBlobAsChunks(inputStream, outputStream);
      outputStream.flush();
      int count = updateInstallationCountForProduct(productId, null);
      log.debug("File {} downloaded, installation count incremented to {}", productId, count);
    } catch (IOException e) {
      log.error("Error streaming file for product {}: {}", productId, e.getMessage(), e);
    }
    return outputStream;
  }
}
