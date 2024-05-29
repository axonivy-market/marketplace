package com.axonivy.market.config;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi customHeaderOpenApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .addOpenApiCustomizer(customGlobalHeaders())
                .build();
    }

    private OpenApiCustomizer customGlobalHeaders() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            Parameter headerParameter = new Parameter()
                    .in("header")
                    .schema(new StringSchema())
                    .name("X-Requested-By")
                    .description("ivy")
                    .required(true);

            operation.addParametersItem(headerParameter);
        }));
    }
}
