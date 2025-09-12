package com.axonivy.market.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GitHubReleaseModelTest {

  @Test
  void testEqualsAndHashCodeSameName() {
    GitHubReleaseModel release1 = new GitHubReleaseModel();
    release1.setName("12.0.3");

    GitHubReleaseModel release2 = new GitHubReleaseModel();
    release2.setName("12.0.3");

    assertEquals(release1, release2, "Objects with the same name should be equal");
    assertEquals(release1.hashCode(), release2.hashCode(),
        "Objects with the same name should have the same hashCode");
  }

  @Test
  void testEqualsAndHashCodeDifferentName() {
    GitHubReleaseModel release1 = new GitHubReleaseModel();
    release1.setName("12.0.3");

    GitHubReleaseModel release2 = new GitHubReleaseModel();
    release2.setName("12.0.4");

    assertNotEquals(release1, release2, "Objects with different names should not be equal");
    assertNotEquals(release1.hashCode(), release2.hashCode(),
        "Objects with different names should not have the same hashCode");
  }

  @Test
  void testEqualsWithNullAndDifferentClass() {
    GitHubReleaseModel release1 = new GitHubReleaseModel();
    GitHubReleaseModel release2 = null;
    var release3 = "string";
    release1.setName("12.0.3");

    assertNotEquals(release1, release2, "GitHubReleaseModel should not equal null");
    assertNotEquals(release1, release3,
        "GitHubReleaseModel should not equal an object of a different class");
  }
}
