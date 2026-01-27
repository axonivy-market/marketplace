package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.service.CoreVersionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.axonivy.market.core.constants.ProductJsonConstants.NAME;

@Log4j2
@Service
@AllArgsConstructor
public class CoreVersionServiceImpl implements CoreVersionService {
  private final CoreProductJsonContentRepository coreProductJsonRepo;
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version,
      String designerVersion) {
    Map<String, Object> result = new HashMap<>();
    try {
      var productJsonContent =
          coreProductJsonRepo.findByProductIdAndVersion(productId, version).stream().findAny().orElse(null);
      if (ObjectUtils.isEmpty(productJsonContent)) {
        return new HashMap<>();
      }
      result = mapper.readValue(productJsonContent.getContent(), Map.class);
      result.computeIfAbsent(NAME, k -> productJsonContent.getName());
    } catch (JsonProcessingException jsonProcessingException) {
      log.error(jsonProcessingException);
    }
    return result;
  }
}
