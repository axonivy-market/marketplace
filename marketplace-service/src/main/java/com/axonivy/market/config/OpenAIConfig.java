package com.axonivy.market.config;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
  
  @Value("${market.chatbot.api-key}")
  private String apiKey;
  
  @Value("${market.chatbot.endpoint}")
  private String endpoint;

  @Value("${market.chatbot.deployment-name}")
  private String deploymentName;

  @Value("${market.azure.search-endpoint}")
  private  String searchEndpoint;

  @Value("${market.azure.search.index-name}")
  private  String indexName;

  @Value("${market.azure.search.key}")
  private  String searchKey;

  @Bean
  public OpenAIClient openAIClient() {
    return new OpenAIClientBuilder()
        .credential(new AzureKeyCredential(apiKey))
        .endpoint(endpoint)
        .buildClient();
  }

  @Bean
  public SearchClient searchClient() {
    return new SearchClientBuilder()
        .endpoint(searchEndpoint)
        .indexName(indexName)
        .credential(new AzureKeyCredential(searchKey))
        .buildClient();
  }

  @Bean
  public String chatbotDeploymentName() {
    return deploymentName;
  }
}