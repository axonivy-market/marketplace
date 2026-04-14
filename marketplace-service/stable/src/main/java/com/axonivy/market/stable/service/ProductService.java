package com.axonivy.market.stable.service;

import com.axonivy.market.core.service.CoreProductService;

public interface ProductService extends CoreProductService {

  String getBestMatchVersion(String id, String version, Boolean isShowDevVersion);
}
