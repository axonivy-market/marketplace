package com.axonivy.market.core.config;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenApiCustomizer fixWildcardContentTypes() {
    return openApi -> {
      if (openApi.getPaths() == null) return;

      openApi.getPaths().values().forEach(pathItem ->
          pathItem.readOperations().forEach(operation -> {
            if (operation.getResponses() == null) return;

            operation.getResponses().values().forEach(apiResponse -> {
              Content content = apiResponse.getContent();
              if (content == null) return;

              MediaType wildcard = content.get("*/*");
              if (wildcard != null) {
                content.remove("*/*");
                content.addMediaType("application/json", wildcard);
              }
            });
          })
      );
    };
  }
}
