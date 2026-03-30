package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.factory.CoreVersionFactory;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.impl.CoreProductServiceImpl;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.stable.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@Primary
public class ProductServiceImpl extends CoreProductServiceImpl implements ProductService {
  private final CoreMetadataRepository coreMetadataRepo;
  private final CoreProductRepository coreProductRepository;

  public ProductServiceImpl(CoreProductRepository coreProductRepo, CoreMetadataRepository coreMetadataRepo,
      CoreProductRepository coreProductRepository) {
    super(coreProductRepo);
    this.coreMetadataRepo = coreMetadataRepo;
    this.coreProductRepository = coreProductRepository;
  }

  @Override
  public String fetchBestMatchVersion(String id, String version) {
    if(!coreProductRepository.existsById(id)) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id);
    }

    List<String> installableVersions = CoreVersionUtils.getInstallableVersionsFromMetadataList(
        coreMetadataRepo.findByProductId(id));
    return CoreVersionUtils.getBestMatchVersion(installableVersions, version);
  }

  @Override
  public String getBestMatchVersion(String id, String version, Boolean isShowDevVersion) {
    if(!coreProductRepository.existsById(id)) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id);
    }

    List<String> versions = CoreVersionUtils.getVersionsToDisplay(coreProductRepository.getReleasedVersionsById(id),
        isShowDevVersion);
    return CoreVersionFactory.get(versions, version);
  }
}
