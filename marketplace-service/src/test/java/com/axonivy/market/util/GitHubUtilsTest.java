package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.github.util.GitHubUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.Link;

import java.io.IOException;

import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GitHubUtilsTest extends BaseSetup {

  @Test
  void testSortMetaJsonFirst() {
    int result = GitHubUtils.sortMetaJsonFirst(META_FILE, LOGO_FILE);
    Assertions.assertEquals(-1, result);

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, META_FILE);
    Assertions.assertEquals(1, result);

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, LOGO_FILE);
    Assertions.assertEquals(0, result);
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModel() throws IOException {
    String productId = "test-product-id";
    long ghReleaseId = 12345L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result);
    assertEquals("self", result.getRel().value());
    String href = result.getHref();
    assertNotNull(href);
    assertTrue(href.contains(productId));
    assertTrue(href.contains(String.valueOf(ghReleaseId)));
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModel_withSpecialCharacters() throws IOException {
    String productId = "test-product-with-special-chars";
    long ghReleaseId = 98765L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result);
    assertEquals("self", result.getRel().value());
    String href = result.getHref();
    assertNotNull(href);
    assertTrue(href.contains(productId));
    assertTrue(href.contains(String.valueOf(ghReleaseId)));
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModel_withZeroReleaseId() throws IOException {
    String productId = "test-product";
    long ghReleaseId = 0L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result);
    assertEquals("self", result.getRel().value());
    String href = result.getHref();
    assertNotNull(href);
    assertTrue(href.contains(productId));
    assertTrue(href.contains("0"));
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModel_withLargeReleaseId() throws IOException {
    String productId = "test-product";
    long ghReleaseId = Long.MAX_VALUE;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result);
    assertEquals("self", result.getRel().value());
    String href = result.getHref();
    assertNotNull(href);
    assertTrue(href.contains(productId));
    assertTrue(href.contains(String.valueOf(Long.MAX_VALUE)));
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModel_withEmptyProductId() throws IOException {
    String productId = "";
    long ghReleaseId = 12345L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result);
    assertEquals("self", result.getRel().value());
    String href = result.getHref();
    assertNotNull(href);
    assertTrue(href.contains(String.valueOf(ghReleaseId)));
  }
}
