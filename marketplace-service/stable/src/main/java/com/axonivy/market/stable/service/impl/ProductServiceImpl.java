package com.axonivy.market.stable.service.impl;

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

  public ProductServiceImpl(CoreProductRepository coreProductRepo, CoreMetadataRepository coreMetadataRepo) {
    super(coreProductRepo);
    this.coreMetadataRepo = coreMetadataRepo;
  }

  @Override
  public String fetchBestMatchVersion(String id, String version) {
    List<String> installableVersions = CoreVersionUtils.getInstallableVersionsFromMetadataList(
        coreMetadataRepo.findByProductId(id));
    return CoreVersionUtils.getBestMatchVersion(installableVersions, version);
  }
}
