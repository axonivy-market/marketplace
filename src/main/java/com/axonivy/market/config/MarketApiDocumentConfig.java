package com.axonivy.market.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import static com.axonivy.market.constants.CommonConstants.*;

@Configuration
public class MarketApiDocumentConfig {
  private static final String DEFAULT_DOC_GROUP = "api";
  private static final String PATH_PATTERN = "/api/**";
  private static final String DEFAULT_PARAM = "ivy";
  private static final String HEADER_PARAM = "header";

  @Bean
  public GroupedOpenApi buildMarketCustomHeader() {
    return GroupedOpenApi.builder()
        .group(DEFAULT_DOC_GROUP)
        .addOpenApiCustomizer(customMarketHeaders())
        .pathsToMatch(PATH_PATTERN)
        .build();
  }

  private OpenApiCustomizer customMarketHeaders() {
    return openApi -> openApi.getPaths().values().forEach((PathItem pathItem) -> {
      for (Operation operation : pathItem.readOperations()) {
        Parameter headerParameter = new Parameter().in(HEADER_PARAM)
            .schema(new StringSchema()).name(REQUESTED_BY)
            .description(DEFAULT_PARAM).required(true);
        operation.addParametersItem(headerParameter);
      }
    });
  }
}