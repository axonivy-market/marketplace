package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProductDependencyTest {
  private static ProductDependency build(String productId, String artifactId, String version) {
    return ProductDependency.builder()
        .productId(productId)
        .artifactId(artifactId)
        .version(version)
        .build();
  }

  private static ProductDependency createPortalComponentVersion10Dependency() {
    return build("portal", "portal-components", "10.0.0");
  }

  private static ProductDependency createPortalComponentVersion11Dependency() {
    return build("portal", "portal-components", "11.0.0");
  }

  @Test
  void testEqualsSameFieldsShouldBeEqual() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = createPortalComponentVersion10Dependency();
    assertEquals(dependencyA, dependencyB,
        "Two instances with same productId/artifactId/version must be equal");
  }

  @Test
  void testEqualsDifferentVersionShouldNotBeEqual() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = createPortalComponentVersion11Dependency();
    assertNotEquals(dependencyA, dependencyB, "Different version must result in not-equal");
  }

  @Test
  void testEqualsDifferentArtifactIdShouldNotBeEqual() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = build("portal", "portal", "10.0.0");
    assertNotEquals(dependencyA, dependencyB, "Different artifactId must result in not-equal");
  }

  @Test
  void testEqualsDifferentProductIdShouldNotBeEqual() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = build("other-product", "portal-components", "10.0.0");
    assertNotEquals(dependencyA, dependencyB, "Different productId must result in not-equal");
  }

  @Test
  void testEqualsNonProductDependencyObjectShouldReturnFalse() {
    var dependency = createPortalComponentVersion10Dependency();
    assertNotEquals("test", dependency, "Comparing with a String object should return false");
  }

  @Test
  void testHashCodeEqualObjectsShouldHaveSameHashCode() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = createPortalComponentVersion10Dependency();
    assertEquals(dependencyA.hashCode(), dependencyB.hashCode(), "Equal objects must have the same hash code");
  }

  @Test
  void testHashCodeDifferentObjectsShouldUsuallyHaveDifferentHashCode() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = createPortalComponentVersion11Dependency();
    assertNotEquals(dependencyA.hashCode(), dependencyB.hashCode(),
        "Different objects should typically have different hash codes");
  }

  @Test
  void testEqualsAndHashCodeUsableInSet() {
    var dependencyA = createPortalComponentVersion10Dependency();
    var dependencyB = createPortalComponentVersion10Dependency();
    var set = new HashSet<ProductDependency>();
    set.add(dependencyA);
    set.add(dependencyB);
    assertEquals(1, set.size(), "A Set must deduplicate equal ProductDependency instances");
  }

}
