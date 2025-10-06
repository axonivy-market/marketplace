package com.axonivy.market.bo;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackTest {
  @Test
  void testSetContentTrimsWhitespace() {
    Feedback feedback = new Feedback();
    feedback.setContent("  some content  ");
    assertEquals("some content", feedback.getContent(),
        "Content should be trimmed of leading and trailing whitespace");
  }

  @Test
  void testSetContentWithNull() {
    Feedback feedback = new Feedback();
    feedback.setContent(null);
    assertNull(feedback.getContent(),
        "Content should be null when setting a null value");
  }

  @Test
  void testDefaultValues() {
    Feedback feedback = new Feedback();
    assertNull(feedback.getUserId(), "UserId should be null by default");
    assertNull(feedback.getProductId(), "ProductId should be null by default");
    assertNull(feedback.getProductNames(), "ProductNames should be null by default");
    assertNull(feedback.getContent(), "Content should be null by default");
    assertNull(feedback.getRating(), "Rating should be null by default");
    assertNull(feedback.getFeedbackStatus(), "FeedbackStatus should be null by default");
    assertNull(feedback.getModeratorName(), "ModeratorName should be null by default");
    assertNull(feedback.getReviewDate(), "ReviewDate should be null by default");
    assertNull(feedback.getVersion(), "Version should be null by default");
    assertNull(feedback.getIsLatest(), "isLatest should be null by default");
  }

  @Test
  void testSettersAndGetters() {
    Feedback feedback = new Feedback();
    LocalDateTime now = LocalDateTime.now();
    Map<String, String> productNames = Map.of("p1", "Product One");

    feedback.setUserId("user123");
    feedback.setProductId("product456");
    feedback.setProductNames(productNames);
    feedback.setContent("content");
    feedback.setRating(5);
    feedback.setFeedbackStatus(FeedbackStatus.APPROVED);
    feedback.setModeratorName("admin");
    feedback.setReviewDate(now);
    feedback.setVersion(2);
    feedback.setIsLatest(Boolean.TRUE);

    assertEquals("user123", feedback.getUserId(), "UserId should match the value set");
    assertEquals("product456", feedback.getProductId(), "ProductId should match the value set");
    assertEquals(productNames, feedback.getProductNames(), "ProductNames should match the map set");
    assertEquals("content", feedback.getContent(), "Content should match the value set");
    assertEquals(5, feedback.getRating(), "Rating should match the value set");
    assertEquals(FeedbackStatus.APPROVED, feedback.getFeedbackStatus(), "FeedbackStatus should match the value set");
    assertEquals("admin", feedback.getModeratorName(), "ModeratorName should match the value set");
    assertEquals(now, feedback.getReviewDate(), "ReviewDate should match the value set");
    assertEquals(2, feedback.getVersion(), "Version should match the value set");
    assertTrue(feedback.getIsLatest(), "isLatest should match the value set (true)");
  }
}
