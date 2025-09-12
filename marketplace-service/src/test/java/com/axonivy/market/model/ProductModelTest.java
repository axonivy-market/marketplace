package com.axonivy.market.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ProductModelTest {
  @Test
  void testEqualsAndHashCodeSameId() {
    ProductModel model1 = new ProductModel();
    model1.setId("jira-connector");

    ProductModel model2 = new ProductModel();
    model2.setId("jira-connector");

    assertEquals(model1, model2, "Models with same id should be equal");
    assertEquals(model1.hashCode(), model2.hashCode(), "Hash codes should be equal for same id");
  }

  @Test
  void testEqualsDifferentId() {
    ProductModel model1 = new ProductModel();
    model1.setId("jira-connector");

    ProductModel model2 = new ProductModel();
    model2.setId("amazon-comprehend");

    assertNotEquals(model1, model2, "Models with different ids should not be equal");
  }

  @Test
  void testEqualsNullAndDifferentClass() {
    ProductModel model = new ProductModel();
    model.setId("jira-connector");

    assertNotEquals(model, null, "Model should not be equal to null");
    assertNotEquals(model, "random-string", "Model should not be equal to different class type");
  }
}
