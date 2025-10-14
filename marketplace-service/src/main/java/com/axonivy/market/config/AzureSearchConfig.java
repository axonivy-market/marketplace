package com.axonivy.market.config;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureSearchConfig {

  @Value("${market.azure.search-endpoint}")
  private String searchEndpoint;

  @Value("${market.azure.search.index-name}")
  private String indexName;

  @Value("${market.azure.search.key}")
  private String searchKey;

  @Bean
  public SearchClient searchClient() {
    return new SearchClientBuilder()
        .endpoint(searchEndpoint)
        .indexName(indexName)
        .credential(new AzureKeyCredential(searchKey))
        .buildClient();
  }
}
