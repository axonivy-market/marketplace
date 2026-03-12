package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.repository.CoreGithubRepoRepository;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.impl.CoreProductServiceImpl;
import com.axonivy.market.stable.service.ProductService;
import com.axonivy.market.stable.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ProductServiceImpl extends CoreProductServiceImpl implements ProductService {
  public ProductServiceImpl(CoreProductRepository coreProductRepo, CoreMetadataRepository coreMetadataRepo,
      CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepository,
      CoreProductJsonContentRepository coreProductJsonContentRepo,
      CoreGithubRepoRepository coreGithubRepository,
      CoreMetadataRepository coreMetadataRepository,
      ProductModuleContentRepository productModuleContentRepo, GHAxonIvyMarketRepoService axonIvyMarketRepoService,
      GHAxonIvyProductRepoService axonIvyProductRepoService, GitHubRepoMetaRepository gitHubRepoMetaRepo,
      GitHubService gitHubService, MetadataRepository metadataRepo, ProductJsonContentRepository productJsonContentRepo,
      ImageRepository imageRepo, ImageService imageService, ProductContentService productContentService,
      ExternalDocumentService externalDocumentService, MetadataService metadataService,
      ProductMarketplaceDataService productMarketplaceDataService,
      ProductMarketplaceDataRepository productMarketplaceDataRepo,
      MavenArtifactVersionRepository mavenArtifactVersionRepository, FileDownloadService fileDownloadService,
      VersionService versionService, GithubRepoRepository githubRepo) {
    super(coreProductRepo, coreMetadataRepository, productMarketplaceDataService, mavenArtifactVersionRepository,
        productJsonContentRepo, githubRepo, versionService);
  }
}
