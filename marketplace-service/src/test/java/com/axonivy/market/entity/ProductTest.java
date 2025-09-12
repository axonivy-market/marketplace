package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ProductTest {

  @Test
  void testEqualsWithNullObject() {
    Product product1 = new Product();
    Product product2 = null;
    product1.setId("p1");

    assertNotEquals(product1, product2, "Product should not be equal to null");
  }

  @Test
  void testEqualsWithDifferentClass() {
    Product product = new Product();
    product.setId("p1");

    assertNotEquals(product, "string", "Product should not be equal to object of another class");
  }

  @Test
  void testEqualsSameId() {
    Product product1 = new Product();
    product1.setId("p1");

    Product product2 = new Product();
    product2.setId("p1");

    assertEquals(product1, product2, "Products with the same ID should be equal");
    assertEquals(product1.hashCode(), product2.hashCode(),
        "Products with the same ID should have the same hashCode");
  }

  @Test
  void testEqualsDifferentId() {
    Product product1 = new Product();
    product1.setId("p1");

    Product product2 = new Product();
    product2.setId("p2");

    assertNotEquals(product1, product2, "Products with different IDs should not be equal");
    assertNotEquals(product1.hashCode(), product2.hashCode(),
        "Products with different IDs should ideally have different hashCodes");
  }

  @Test
  void testEqualsBothNullIds() {
    Product product1 = new Product();
    Product product2 = new Product();

    assertEquals(product1, product2, "Products with both IDs null should be equal");
    assertEquals(product1.hashCode(), product2.hashCode(),
        "Products with both IDs null should have the same hashCode");
  }
}
