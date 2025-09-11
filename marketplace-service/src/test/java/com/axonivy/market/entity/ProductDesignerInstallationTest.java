package com.axonivy.market.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProductDesignerInstallationTest {

  @Test
  void testEqualsSameProductIdReturnsTrue() {
    var i1 = new ProductDesignerInstallation("p4", "v4", 40);
    var i2 = new ProductDesignerInstallation("p4", "v5", 50);

    assertEquals(i1, i2, "Expected objects with same productId to be equal");
    assertEquals(i1.hashCode(), i2.hashCode(), "Expected objects with same productId to have same hashCode");
  }

  @Test
  void testEqualsDifferentProductIdReturnsFalse() {
    var i1 = new ProductDesignerInstallation("p5", "v5", 50);
    var i2 = new ProductDesignerInstallation("p6", "v5", 50);

    assertNotEquals(i1, i2, "Expected objects with different productId to not be equal");
  }

  @Test
  void testEqualsNullObjectReturnsFalse() {
    var installation = new ProductDesignerInstallation("p7", "v7", 70);

    assertNotEquals(null, installation, "Expected equals(null) to return false");
  }

  @Test
  void testEqualsDifferentClassReturnsFalse() {
    var installation = new ProductDesignerInstallation("p8", "v8", 80);

    assertNotEquals("random string", installation, "Expected equals with different class to return false");
  }
}
