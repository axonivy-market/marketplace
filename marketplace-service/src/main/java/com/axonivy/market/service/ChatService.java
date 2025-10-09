package com.axonivy.market.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired
    private OpenAIClient openAIClient;

    @Autowired(required = false)
    private SearchClient searchClient;
    
    @Autowired
    @Qualifier("chatbotDeploymentName")
    private String deploymentName;

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant for the **Axon Ivy Marketplace**.
            
            **Your Role:**
            - Help users find and understand Marketplace products.
            - Explain installation procedures and compatibility requirements.
            - Provide information about:
              - Available products and their features
              - Vendor details and support options  
              - Product categories and search functions  
              - Documentation and source code links  
            
            **Instructions:**
            - Always reply in **Markdown** format.
            - Use **clear headings**, **bullet points**, **bold text**, and **links** when appropriate.
            - Keep answers **short, clear, and under 200 words**.
            - **Break down complex topics** into simple, step-by-step explanations.
            - **Clarify user questions** if they are ambiguous.
            - Maintain a **friendly, concise, and helpful tone**.
            - If you don’t know something specific about a product, suggest checking the **Axon Ivy Marketplace** or **contacting the vendor**.
            
            """;
    public String getChatResponse(String userMessage) {
        try {
            String searchContext = searchRelevantContent(userMessage);
            
            String enhancedSystemPrompt = buildEnhancedPrompt(searchContext);
            
            List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(enhancedSystemPrompt),
                new ChatRequestUserMessage(userMessage)
            );
            
            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setTemperature(0.7);
            options.setTopP(0.9);
            options.setFrequencyPenalty(0.0);
            options.setPresencePenalty(0.0);

            ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, options);
            
            if (!chatCompletions.getChoices().isEmpty()) {
                ChatChoice choice = chatCompletions.getChoices().get(0);
                ChatResponseMessage responseMessage = choice.getMessage();
                
                logger.info("Chat request processed. Model: {}, Usage: {} tokens, Search results: {}", 
                    chatCompletions.getId(), 
                    chatCompletions.getUsage().getTotalTokens(),
                    !searchContext.isEmpty() ? "included" : "none");
                
                return responseMessage.getContent();
            } else {
                logger.warn("No response choices returned from OpenAI");
                return "I'm sorry, I couldn't generate a response at the moment. Please try again.";
            }
            
        } catch (Exception e) {
            logger.error("Error getting chat response: {}", e.getMessage(), e);
            return "I'm experiencing technical difficulties. Please try again later.";
        }
    }
    
    private String searchRelevantContent(String userMessage) {
        try {
            if (searchClient == null) {
                logger.warn("SearchClient is not configured, skipping search");
                return "";
            }

            SearchOptions searchOptions = new SearchOptions()
                .setTop(5)
                .setIncludeTotalCount(true);
           searchOptions.setFilter("ivyVersion eq 'market'");

            SearchPagedIterable results = searchClient.search(userMessage, searchOptions, Context.NONE);

            StringBuilder contextBuilder = new StringBuilder();
            int resultCount = 0;
            ObjectMapper mapper = new ObjectMapper();
            
            for (SearchResult result : results) {
                try {
                    resultCount++;
                    
                    var document = result.getDocument(Object.class);
                    
                    String documentJson = mapper.writeValueAsString(document);
                    
                    contextBuilder.append("Search Result ").append(resultCount).append(":\n");
                    contextBuilder.append("Score: ").append(result.getScore()).append("\n");
                    contextBuilder.append("Document: ").append(documentJson).append("\n\n");
                    
                    logger.debug("Search result {}: Score={}, Document={}", resultCount, result.getScore(), documentJson);
                    
                } catch (Exception e) {
                    logger.warn("Error processing search result {}: {}", resultCount, e.getMessage());
                }
            }
            
            String searchContext = contextBuilder.toString();
            logger.info("Azure Search returned {} relevant results for query: '{}'", resultCount, userMessage);
            
            if (resultCount == 0) {
                logger.warn("No search results found for query: '{}'", userMessage);
            }
            
            return searchContext;
            
        } catch (Exception e) {
            logger.error("Error searching Azure Search: {}", e.getMessage(), e);
            return "";
        }
    }
    
    private String buildEnhancedPrompt(String searchContext) {
        if (searchContext.isEmpty()) {
            return SYSTEM_PROMPT;
        }
        
        return SYSTEM_PROMPT + """
            
            Here is relevant information from the marketplace search:
            
            """ + searchContext + """
            
            Use this information to provide accurate and specific answers about marketplace products.
            If the search results contain relevant information, reference it in your response.
            If no relevant information is found, provide general guidance and suggest the user check the marketplace directly.
            """;
    }
    
    public String getChatResponseWithContext(String userMessage, String productContext) {
        try {
            String searchContext = searchRelevantContent(userMessage);
            
            String combinedContext = "";
            if (!productContext.isEmpty() && !searchContext.isEmpty()) {
                combinedContext = "Provided context:\n" + productContext + "\n\nSearch results:\n" + searchContext;
            } else if (!productContext.isEmpty()) {
                combinedContext = productContext;
            } else if (!searchContext.isEmpty()) {
                combinedContext = searchContext;
            }
            
            String enhancedPrompt = combinedContext.isEmpty() ? 
                SYSTEM_PROMPT : 
                buildEnhancedPrompt(combinedContext);
            
            List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(enhancedPrompt),
                new ChatRequestUserMessage(userMessage)
            );
            
            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setTemperature(0.7);
            options.setTopP(0.9);
            options.setFrequencyPenalty(0.0);
            options.setPresencePenalty(0.0);
            
            ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, options);
            
            if (!chatCompletions.getChoices().isEmpty()) {
                ChatChoice choice = chatCompletions.getChoices().get(0);
                ChatResponseMessage responseMessage = choice.getMessage();
                
                logger.info("Chat with context processed. Model: {}, Usage: {} tokens", 
                    chatCompletions.getId(), 
                    chatCompletions.getUsage().getTotalTokens());
                
                return responseMessage.getContent();
            } else {
                logger.warn("No response choices returned from OpenAI");
                return "I'm sorry, I couldn't generate a response at the moment. Please try again.";
            }
            
        } catch (Exception e) {
            logger.error("Error getting chat response with context: {}", e.getMessage(), e);
            return "I'm experiencing technical difficulties. Please try again later.";
        }
    }
    
    public String searchProducts(String query) {
        return searchRelevantContent(query);
    }
}