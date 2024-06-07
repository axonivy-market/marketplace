package com.axonivy.market.service;

import com.axonivy.market.model.ProductDetailArtifactModel;

import java.io.IOException;

public interface ProductDetailActionService {
    ProductDetailArtifactModel getArtifacts(String productId, Boolean isShowDevVersion, String designerVersion) throws IOException;
}
