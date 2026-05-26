package com.axonivy.market.core.factory;

import com.axonivy.market.core.strategy.VersionMatchStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreVersionFactoryTest {
  @Mock
  VersionMatchStrategy matchStrategy;

  @Test
  void testFindVersionStartWithOrNullHappy() {
    List<String> releaseVersions = List.of("10.0.0", "11.0.0");
    String result = CoreVersionFactory.findVersionStartWithOrNull(releaseVersions, "10");
    assertEquals("10.0.0", result);
  }

  @Test
  void testFindVersionStartWithOrNullEmptyListReturnsInput() {
    String result = CoreVersionFactory.findVersionStartWithOrNull(List.of(), "10.0");
    assertEquals("10.0", result);
  }

  @Test
  void testFindVersionStartWithOrNullNotFoundReturnsNull() {
    List<String> releaseVersions = List.of("10.0.0", "11.0.0");
    String result = CoreVersionFactory.findVersionStartWithOrNull(releaseVersions, "12");
    assertNull(result);
  }

  @Test
  void testFindLowerVersionHappy() {
    List<String> releaseVersions = List.of("10.0.0", "9.0.0");
    String result = CoreVersionFactory.findLowerVersion(releaseVersions, "11.0.0");
    // MavenVersionComparator logic: "10.0.0" < "11.0.0"
    assertEquals("10.0.0", result);
  }

  @Test
  void testFindLowerVersionNullList() {
    String result = CoreVersionFactory.findLowerVersion(null, "11.0.0");
    assertNull(result);
  }

  @Test
  void testShouldHandleNullVersions() {
    when(matchStrategy.findMatch(anyList(), eq("1.0")))
        .thenReturn("fallback");

    String result = CoreVersionFactory.get(null, "1.0", matchStrategy);

    assertEquals("fallback", result);
    verify(matchStrategy).findMatch(eq(List.of()), eq("1.0"));
  }

  @Test
  void testShouldFilterNullsAndSortDescending() {
    List<String> versions = Arrays.asList("1.0", null, "2.0");

    when(matchStrategy.findMatch(anyList(), any()))
        .thenAnswer(invocation -> {
          List<String> sorted = invocation.getArgument(0);
          assertEquals(List.of("2.0", "1.0"), sorted,
              "Expected versions to be filtered (no nulls) and sorted descending");
          return "ok";
        });

    CoreVersionFactory.get(versions, "1.0", matchStrategy);
  }

  @Test
  void testShouldReturnLatestReleasedVersionForLatestKeyword() {
    List<String> versions = List.of("2.0-dev", "1.0", "3.0");
    String result = CoreVersionFactory.get(versions, "latest", matchStrategy);

    assertEquals("3.0", result,
        "Expected latest released version when 'latest' keyword is used");

    verifyNoInteractions(matchStrategy);
  }

  @Test
  void testShouldReturnLatestReleasedVersionForSprintKeyword() {
    List<String> versions = List.of("1.0", "2.0");
    String result = CoreVersionFactory.get(versions, "sprint", matchStrategy);

    assertEquals("2.0", result,
        "Expected latest released version when 'sprint' keyword is used");

    verifyNoInteractions(matchStrategy);
  }

  @Test
  void testShouldReturnNewestVersionWhenDevelopmentVersionDetected() {
    List<String> versions = List.of("1.0", "2.0");
    String result = CoreVersionFactory.get(versions, "dev", matchStrategy);

    assertEquals("2.0", result,
        "Expected newest version when a development version is detected");

    verifyNoInteractions(matchStrategy);
  }

  @Test
  void testShouldStripDevPostfix() {
    when(matchStrategy.findMatch(anyList(), eq("10.0"))).thenReturn("10.0");

    CoreVersionFactory.get(List.of("10.0", "11.0"), "10.0-dev", matchStrategy);

    verify(matchStrategy).findMatch(anyList(), eq("10.0"));
  }

  @Test
  void shouldStripDevPrefix() {
    when(matchStrategy.findMatch(anyList(), eq("10.0"))).thenReturn("10.0");

    CoreVersionFactory.get(List.of("10.0", "11.0"), "dev-10.0", matchStrategy);

    verify(matchStrategy).findMatch(anyList(), eq("10.0"));
  }

  @Test
  void shouldDelegateToMatchStrategy() {
    when(matchStrategy.findMatch(anyList(), eq("1.0"))).thenReturn("1.0");

    String result = CoreVersionFactory.get(List.of("1.0", "2.0"), "1.0", matchStrategy);

    assertEquals("1.0", result, "Expected result to come from VersionMatchStrategy");
    verify(matchStrategy).findMatch(anyList(), eq("1.0"));
  }
}
