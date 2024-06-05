package com.axonivy.market.service.impl;

import com.axonivy.market.model.ProductDetailArtifactModel;
import com.axonivy.market.service.ProductDetailActionService;
import com.axonivy.market.service.VersionService;

import java.util.List;

public class ProductDetailActionServiceImpl implements ProductDetailActionService {
    private final VersionService versionService;

    public ProductDetailActionServiceImpl(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    public ProductDetailArtifactModel getArtifacts(String productId, Boolean isShowDevVersion, String designerVersion) {
        List<String> version = versionService.getVersionsToDisplay(productId, isShowDevVersion, designerVersion);
        return null;
    }
}
