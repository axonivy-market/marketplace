package com.axonivy.market.core.service;

import java.util.Map;

public interface CoreVersionService {
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String version, String designerVersion);
}
