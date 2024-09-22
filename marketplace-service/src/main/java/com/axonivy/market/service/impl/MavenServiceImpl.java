package com.axonivy.market.service.impl;

import com.axonivy.market.bo.MavenArtifact;
import com.axonivy.market.bo.MavenVersion;
import com.axonivy.market.entity.MavenVersionSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.MavenVersionSyncRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.MavenService;
import com.axonivy.market.service.VersionService;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MavenServiceImpl implements MavenService {
  private final ProductRepository productRepo;
  private final MavenVersionSyncRepository mavenRepo;

  public MavenServiceImpl(ProductRepository productRepo, MavenVersionSyncRepository mavenRepo) {this.productRepo = productRepo;
    this.mavenRepo = mavenRepo;
  }

  @Override
  public void syncAllArtifactFromMaven() {
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTag();
    products.forEach(product -> {
      List<MavenVersion> mavenVersions = new ArrayList<>();
      List<String> nonSyncedVersions = product.getReleasedVersions();
      MavenVersionSync cache = mavenRepo.findById(product.getId()).orElse(null);
      if(!CollectionUtils.isEmpty(cache.getSyncedVersions())) {
        nonSyncedVersions.removeAll(cache.getSyncedVersions());
      }
      nonSyncedVersions.forEach(version -> {
//        List<MavenArtifact> artifactsInVersion = VersionService.;
      });
    });
  }
}
