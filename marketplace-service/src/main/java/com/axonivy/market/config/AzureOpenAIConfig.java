package com.axonivy.market.config;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AzureOpenAIConfig {

    @Value("${market.chatbot.api-key}")
    private String apiKey;

    @Value("${market.chatbot.endpoint}")
    private String endpoint;

    @Value("${market.chatbot.deployment-name}")
    private String deploymentName;

    @Bean
    public OpenAIClient openAIClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();
    }

    @Bean
    public String chatbotDeploymentName() {
        return deploymentName;
    }
}