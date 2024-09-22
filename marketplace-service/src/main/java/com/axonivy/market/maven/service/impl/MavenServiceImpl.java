package com.axonivy.market.maven.service.impl;

import com.axonivy.market.maven.model.MavenArtifact;
import com.axonivy.market.maven.model.MavenVersion;
import com.axonivy.market.entity.MavenVersionSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.maven.util.MavenUtils;
import com.axonivy.market.repository.MavenVersionSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.maven.service.MavenService;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class MavenServiceImpl implements MavenService {
  private final ProductRepository productRepo;
  private final MavenVersionSyncRepository mavenRepo;
  private final ProductJsonContentRepository productJsonRepo;

  public MavenServiceImpl(ProductRepository productRepo, MavenVersionSyncRepository mavenRepo,
      ProductJsonContentRepository productJsonRepo) {this.productRepo = productRepo;
    this.mavenRepo = mavenRepo;
    this.productJsonRepo = productJsonRepo;
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
        ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(product.getId(), version);

        List<MavenArtifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
      });
    });
  }
}
