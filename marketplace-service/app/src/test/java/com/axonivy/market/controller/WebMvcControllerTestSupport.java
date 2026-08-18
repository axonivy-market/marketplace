package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.service.AppSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

abstract class WebMvcControllerTestSupport extends BaseSetup {

  @Autowired
  protected MockMvc mockMvc;

  @MockitoBean
  protected AppSettingService appSettingService;

  @MockitoBean
  protected JpaMetamodelMappingContext jpaMetamodelMappingContext;

  protected RequestPostProcessor requestedByHeader() {
    return request -> {
      request.addHeader(CoreCommonConstants.REQUESTED_BY, CoreCommonConstants.MARKET_WEBSITE);
      return request;
    };
  }
}
