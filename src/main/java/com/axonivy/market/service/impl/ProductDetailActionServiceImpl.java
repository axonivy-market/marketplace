package com.axonivy.market.service.impl;

import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.model.ProductDetailArtifactModel;
import com.axonivy.market.service.ProductDetailActionService;
import com.axonivy.market.service.VersionService;

import java.util.List;

public class ProductDetailActionServiceImpl implements ProductDetailActionService {
    private final VersionService versionService;
    private final GHAxonIvyMarketRepoService gitHubService;

    public ProductDetailActionServiceImpl(VersionService versionService, GHAxonIvyMarketRepoService gitHubService) {
        this.versionService = versionService;
        this.gitHubService = gitHubService;
    }

    @Override
    public ProductDetailArtifactModel getArtifacts(String productId, Boolean isShowDevVersion, String designerVersion) {
        List<String> versions = versionService.getVersionsToDisplay(productId, isShowDevVersion, designerVersion);
        for (String version : versions) {

        }
        return null;
    }

    public String getProductModuleFromRepoNameAndVersion(String repoName, String Version) {

        return null;
    }


}
