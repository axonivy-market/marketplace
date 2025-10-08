package com.axonivy.market.controller;

import com.axonivy.market.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "AI Chatbot endpoints for marketplace assistance")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping("/message")
    @Operation(
        summary = "Send message to chatbot",
        description = "Send a message to the AI chatbot and get a response about marketplace products and services"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Chat response generated successfully",
        content = @Content(schema = @Schema(implementation = ChatResponse.class))
    )
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            String response = chatService.getChatResponse(request.getMessage());
            
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setMessage(response);
            chatResponse.setSessionId(request.getSessionId());
            chatResponse.setSuccess(true);
            
            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("Sorry, I'm having trouble processing your request right now.");
            errorResponse.setSessionId(request.getSessionId());
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    @GetMapping("/health")
    @Operation(
        summary = "Check chatbot health",
        description = "Check if the chatbot service is working properly"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chatbot service is running");
    }
    
    public static class ChatRequest {
        private String message;
        private String sessionId;
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class ChatResponse {
        private String message;
        private String sessionId;
        private boolean success;
        private String error;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}