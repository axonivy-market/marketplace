package com.axonivy.market.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {
  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper(List<Module> modules) {
    JsonMapper.Builder builder = JsonMapper.builder().findAndAddModules();
    modules.forEach(builder::addModule);
    return builder.build();
  }
}
