package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProductDependencyTest {
  private ProductDependency build(String productId, String artifactId, String version) {
    return ProductDependency.builder()
        .productId(productId)
        .artifactId(artifactId)
        .version(version)
        .build();
  }

  @Test
  void testEqualsSameFieldsShouldBeEqual() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("portal", "portal-components", "10.0.0");
    assertEquals(dependencyA, dependencyB,
        "Two instances with same productId/artifactId/version must be equal");
  }

  @Test
  void testEqualsDifferentVersionShouldNotBeEqual() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("portal", "portal-components", "11.0.0");
    assertNotEquals(dependencyA, dependencyB, "Different version must result in not-equal");
  }

  @Test
  void testEqualsDifferentArtifactIdShouldNotBeEqual() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("portal", "portal", "10.0.0");
    assertNotEquals(dependencyA, dependencyB, "Different artifactId must result in not-equal");
  }

  @Test
  void testEqualsDifferentProductIdShouldNotBeEqual() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("other-product", "portal-components", "10.0.0");
    assertNotEquals(dependencyA, dependencyB, "Different productId must result in not-equal");
  }

  @Test
  void testEqualsNonProductDependencyObjectShouldReturnFalse() {
    var dependency = build("portal", "portal-components", "10.0.0");
    assertNotEquals(dependency, "test", "Comparing with a String object should return false");
  }

  @Test
  void testHashCodeEqualObjectsShouldHaveSameHashCode() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("portal", "portal-components", "10.0.0");
    assertEquals(dependencyA.hashCode(), dependencyB.hashCode(), "Equal objects must have the same hash code");
  }

  @Test
  void testHashCodeDifferentObjectsShouldUsuallyHaveDifferentHashCode() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("portal", "portal-components", "11.0.0");
    assertNotEquals(dependencyA.hashCode(), dependencyB.hashCode(),
        "Different objects should typically have different hash codes");
  }

  @Test
  void testEqualsAndHashCodeUsableInSet() {
    var dependencyA = build("portal", "portal-components", "10.0.0");
    var dependencyB = build("portal", "portal-components", "10.0.0");
    var set = new HashSet<ProductDependency>();
    set.add(dependencyA);
    set.add(dependencyB);
    assertEquals(1, set.size(), "A Set must deduplicate equal ProductDependency instances");
  }

}
