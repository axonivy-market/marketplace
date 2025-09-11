package com.axonivy.market.model;

import com.axonivy.market.enums.FeedbackStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public interface FeedbackProjection {
  String getId();

  String getUserId();

  String getProductId();

  String getContent();

  Integer getRating();

  FeedbackStatus getFeedbackStatus();

  String getModeratorName();

  LocalDateTime getReviewDate();

  Integer getVersion();

  Date getCreatedAt();

  Date getUpdatedAt();

  // Retrieve as JSON String
  @JsonProperty("productNames")
  String getProductNamesJson();

  default Map<String, String> getProductNames() {
    try {
      var objectMapper = new ObjectMapper();
      return objectMapper.readValue(this.getProductNamesJson(), new TypeReference<>() {
      });
    } catch (Exception e) {
      return Map.of();
    }
  }
}
