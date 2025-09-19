package com.axonivy.market.model;

import com.axonivy.market.enums.FeedbackStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackProjectionTest {
  static class TestFeedbackProjection implements FeedbackProjection {
    private final String productNamesJson;

    TestFeedbackProjection(String productNamesJson) {
      this.productNamesJson = productNamesJson;
    }

    @Override
    public String getId() {return "id";}

    @Override
    public String getUserId() {return "user";}

    @Override
    public String getProductId() {return "product";}

    @Override
    public String getContent() {return "content";}

    @Override
    public Integer getRating() {return 5;}

    @Override
    public FeedbackStatus getFeedbackStatus() {return FeedbackStatus.APPROVED;}

    @Override
    public String getModeratorName() {return "moderator";}

    @Override
    public LocalDateTime getReviewDate() {return LocalDateTime.now();}

    @Override
    public Integer getVersion() {return 1;}

    @Override
    public Date getCreatedAt() {return new Date();}

    @Override
    public Date getUpdatedAt() {return new Date();}

    @Override
    public String getProductNamesJson() {return productNamesJson;}
  }

  @Test
  void testGetProductNamesWithValidJson() {
    String json = "{\"p1\":\"Product One\",\"p2\":\"Product Two\"}";
    FeedbackProjection projection = new TestFeedbackProjection(json);

    Map<String, String> result = projection.getProductNames();

    assertEquals(2, result.size(), "Expected map to contain 2 entries for valid JSON");
    assertEquals("Product One", result.get("p1"), "Expected 'p1' key to map to 'Product One'");
    assertEquals("Product Two", result.get("p2"), "Expected 'p2' key to map to 'Product Two'");
  }

  @Test
  void testGetProductNamesWithInvalidJsonReturnsEmptyMap() {
    String invalidJson = "{not valid json}";
    FeedbackProjection projection = new TestFeedbackProjection(invalidJson);

    Map<String, String> result = projection.getProductNames();

    assertTrue(result.isEmpty(), "Expected empty map for invalid JSON");
  }
}
