package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GithubUserTest {
  @Test
  void testEqualsSameObject() {
    GithubUser user1 = new GithubUser();
    user1.setId("1");

    GithubUser user2 = new GithubUser();
    user2.setId("1");

    assertEquals(user1, user2, "Equals should return true when comparing the same object");
  }

  @Test
  void testEqualsWithNull() {
    GithubUser user = new GithubUser();
    user.setId("1");

    assertNotEquals(null, user, "Equals should return false when comparing with null");
  }

  @Test
  void testEqualsDifferentClass() {
    GithubUser user = new GithubUser();
    user.setId("1");

    assertNotEquals("random string", user,
        "Equals should return false when comparing with different class");
  }

  @Test
  void testEqualsDifferentIds() {
    GithubUser user1 = new GithubUser();
    user1.setId("1");
    GithubUser user2 = new GithubUser();
    user2.setId("2");

    assertNotEquals(user1, user2, "Equals should return false when ids are different");
  }

  @Test
  void testHashCodeConsistency() {
    GithubUser user = new GithubUser();
    user.setId("1");

    int hash1 = user.hashCode();
    int hash2 = user.hashCode();

    assertEquals(hash1, hash2, "HashCode should be consistent across multiple invocations");
  }

  @Test
  void testHashCodeEqualObjects() {
    GithubUser user1 = new GithubUser();
    user1.setId("1");
    GithubUser user2 = new GithubUser();
    user2.setId("1");

    assertEquals(user1.hashCode(), user2.hashCode(),
        "HashCode should be equal when objects have the same id");
  }

  @Test
  void testHashCodeDifferentObjects() {
    GithubUser user1 = new GithubUser();
    user1.setId("1");
    GithubUser user2 = new GithubUser();
    user2.setId("2");

    assertNotEquals(user1.hashCode(), user2.hashCode(),
        "HashCode should differ when objects have different ids");
  }
}
