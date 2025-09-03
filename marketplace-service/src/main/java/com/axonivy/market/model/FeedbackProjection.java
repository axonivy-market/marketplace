package com.axonivy.market.model;

import com.axonivy.market.enums.FeedbackStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  Date getReviewDate();
  Integer getVersion();
  Date getCreatedAt();
  Date getUpdatedAt();

  @JsonProperty("productNames")
  String getProductNamesJson(); // Retrieve as JSON String

  default Map<String, String> getProductNames() {
    try {
      var objectMapper = new ObjectMapper();
      return objectMapper.readValue(this.getProductNamesJson(), new TypeReference<>() {});
    } catch (Exception e) {
      return Map.of();
    }
  }
}
