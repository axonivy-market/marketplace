package com.axonivy.market.service;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;

public interface ProductContentService {
  ProductModuleContent getReadmeAndProductContentsFromVersion(Product product, String version, String url,
      Artifact artifact);
}
