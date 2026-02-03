package com.axonivy.market.stable.service;

import com.axonivy.market.core.service.CoreVersionService;

import java.util.Map;

public interface VersionService extends CoreVersionService {

  Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String designerVersion);
}
