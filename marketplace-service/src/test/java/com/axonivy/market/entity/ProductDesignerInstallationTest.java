package com.axonivy.market.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProductDesignerInstallationTest {

  @Test
  void testEqualsSameProductIdReturnsTrue() {
    var installation1 = new ProductDesignerInstallation("p4", "v4", 40);
    var installation2 = new ProductDesignerInstallation("p4", "v5", 50);

    assertEquals(installation1, installation2, "Expected objects with same productId to be equal");
    assertEquals(installation1.hashCode(), installation2.hashCode(),
        "Expected objects with same productId to have same hashCode");
  }

  @Test
  void testEqualsDifferentProductIdReturnsFalse() {
    var installation1 = new ProductDesignerInstallation("p5", "v5", 50);
    var installation2 = new ProductDesignerInstallation("p6", "v5", 50);

    assertNotEquals(installation1, installation2, "Expected objects with different productId to not be equal");
  }

  @Test
  void testEqualsNullObjectReturnsFalse() {
    var installation = new ProductDesignerInstallation("p7", "v7", 70);

    assertNotNull(installation, "Expected equals(null) to return false");
  }

  @Test
  void testEqualsDifferentClassReturnsFalse() {
    var installation = new ProductDesignerInstallation("p8", "v8", 80);

    assertNotEquals("random string", installation,
        "Expected equals with different class to return false");
  }
}
