package com.axonivy.market.service;

import com.axonivy.market.model.ProductDetailArtifactModel;

public interface ProductDetailActionService {
    ProductDetailArtifactModel getArtifacts(String productId, Boolean isShowDevVersion, String designerVersion);
}
