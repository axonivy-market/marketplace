package com.axonivy.market.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.REQUESTED_BY;

@Configuration
public class MarketApiDocumentConfig {
  private static final String DEFAULT_DOC_GROUP = "api";
  private static final String PATH_PATTERN = "/api/**";
  private static final String DEFAULT_PARAM = "ivy";
  private static final String HEADER_PARAM = "header";

  @Bean
  public GroupedOpenApi buildMarketCustomHeader() {
    return GroupedOpenApi.builder().group(DEFAULT_DOC_GROUP)
            .addOpenApiCustomizer(customMarketHeaders())
            .pathsToMatch(PATH_PATTERN).build();
  }

  private OpenApiCustomizer customMarketHeaders() {
    return openApi -> openApi.getPaths().values().forEach((PathItem pathItem) -> {
      List<Operation> operations = Arrays.asList(pathItem.getPut(), pathItem.getPost(), pathItem.getPatch(), pathItem.getDelete());
      for (Operation operation : operations) {
        if (operation != null) {
          Parameter headerParameter = new Parameter().in(HEADER_PARAM).schema(new StringSchema())
                  .name(REQUESTED_BY).description(DEFAULT_PARAM).required(true);
          operation.addParametersItem(headerParameter);
        }
      }
    });
  }
}