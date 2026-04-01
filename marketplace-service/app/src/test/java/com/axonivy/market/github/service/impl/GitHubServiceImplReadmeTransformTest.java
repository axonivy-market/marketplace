package com.axonivy.market.github.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GitHubServiceImplReadmeTransformTest {

  @Test
  void shouldInsertNoticeBelowFirstHeading() {
    String readme = "# vertexai-google\n\nRead our documentation.";

    String updated = GitHubServiceImpl.insertUnsupportedNoticeBelowFirstHeading(readme);

    assertEquals("# vertexai-google\nProduct is not SUpported anymore\n\nRead our documentation.", updated);
  }

  @Test
  void shouldNotDuplicateNoticeWhenAlreadyBelowHeading() {
    String readme = "# vertexai-google\nProduct is not SUpported anymore\n\nRead our documentation.";

    String updated = GitHubServiceImpl.insertUnsupportedNoticeBelowFirstHeading(readme);

    assertEquals(readme, updated);
  }

  @Test
  void shouldThrowWhenNoHeadingExists() {
    String readme = "No markdown heading here.";

    assertThrows(IllegalArgumentException.class,
        () -> GitHubServiceImpl.insertUnsupportedNoticeBelowFirstHeading(readme));
  }
}

