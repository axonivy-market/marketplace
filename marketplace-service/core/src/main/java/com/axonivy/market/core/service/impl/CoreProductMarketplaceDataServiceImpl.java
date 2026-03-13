package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.repository.CoreProductDesignerInstallationRepository;
import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.CoreProductMarketplaceDataService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class CoreProductMarketplaceDataServiceImpl implements CoreProductMarketplaceDataService {

  private static final int MIN_RANDOM_INSTALLATION_COUNT = 20;
  private static final int MAX_RANDOM_INSTALLATION_COUNT = 50;
  private final CoreProductMarketplaceDataRepository coreProductMarketplaceDataRepo;
  private final CoreProductDesignerInstallationRepository coreProductDesignerInstallationRepo;
  private final CoreProductRepository coreProductRepo;
  private final ObjectMapper mapper = new ObjectMapper();
  private final SecureRandom random = new SecureRandom();
  @Value("${market.legacy.installation.counts.path}")
  private String legacyInstallationCountPath;

  @Override
  public int updateInstallationCountForProduct(String productId, String designerVersion) {
    validateProductExists(productId);
    var productMarketplaceData = getProductMarketplaceData(productId);

    log.info("Increase installation count for product {} By Designer Version {}", productId, designerVersion);
    if (StringUtils.isNotBlank(designerVersion)) {
      coreProductDesignerInstallationRepo.increaseInstallationCountForProductByDesignerVersion(productId, designerVersion);
    }

    log.info("updating installation count for product {}", productId);
    if (BooleanUtils.isTrue(productMarketplaceData.getSynchronizedInstallationCount())) {
      return coreProductMarketplaceDataRepo.increaseInstallationCount(productId);
    }
    int installationCount = getInstallationCountFromFileOrInitializeRandomly(productId);
    return coreProductMarketplaceDataRepo.updateInitialCount(productId, installationCount + 1);
  }

  @Override
  public int updateProductInstallationCount(String id) {
    var productMarketplaceData = getProductMarketplaceData(id);
    if (BooleanUtils.isNotTrue(productMarketplaceData.getSynchronizedInstallationCount())) {
      return coreProductMarketplaceDataRepo.updateInitialCount(id,
          getInstallationCountFromFileOrInitializeRandomly(id));
    }
    return productMarketplaceData.getInstallationCount();
  }

  @Override
  public ProductMarketplaceData getProductMarketplaceData(String productId) {
    return coreProductMarketplaceDataRepo.findById(productId).orElse(
        ProductMarketplaceData.builder().id(productId).build());
  }

  public int getInstallationCountFromFileOrInitializeRandomly(String productId) {
    log.info("synchronizing installation count for product {}", productId);
    var result = 0;
    try {
      var installationCounts = Files.readString(Paths.get(legacyInstallationCountPath));
      Map<String, Integer> mapping = mapper.readValue(installationCounts,
          new TypeReference<HashMap<String, Integer>>() {
          });
      List<String> keyList = mapping.keySet().stream().toList();
      if (keyList.contains(productId)) {
        result = mapping.get(productId);
      } else {
        result = random.nextInt(MIN_RANDOM_INSTALLATION_COUNT, MAX_RANDOM_INSTALLATION_COUNT);
      }
      log.info("synchronized installation count for product {} successfully", productId);
    } catch (IOException ex) {
      log.error("Could not read the marketplace-installation file to synchronize", ex);
    }
    return result;
  }

  private void validateProductExists(String productId) throws NotFoundException {
    if (coreProductRepo.findById(productId).isEmpty()) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
    }
  }
}
