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
    Assertions.assertEquals(-1, result, "Meta file should come before logo file in sort order");

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, META_FILE);
    Assertions.assertEquals(1, result, "Logo file should come after meta file in sort order");

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, LOGO_FILE);
    Assertions.assertEquals(0, result, "Same files should have equal sort order");
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModel() throws IOException {
    String productId = "test-product-id";
    long ghReleaseId = 12345L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result, "Link should not be null");
    assertEquals("self", result.getRel().value(), "Link should have 'self' relation type");
    String href = result.getHref();
    assertNotNull(href, "Link href should not be null");
    assertTrue(href.contains(productId), "Link href should contain the product ID: " + productId);
    assertTrue(href.contains(String.valueOf(ghReleaseId)), "Link href should contain the release ID: " + ghReleaseId);
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModelWithSpecialCharacters() throws IOException {
    String productId = "test-product-with-special-chars";
    long ghReleaseId = 98765L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result, "Link should not be null for product ID with special characters");
    assertEquals("self", result.getRel().value(), "Link should have 'self' relation type for product ID with special characters");
    String href = result.getHref();
    assertNotNull(href, "Link href should not be null for product ID with special characters");
    assertTrue(href.contains(productId), "Link href should contain the product ID with special characters: " + productId);
    assertTrue(href.contains(String.valueOf(ghReleaseId)), "Link href should contain the release ID: " + ghReleaseId);
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModelWithZeroReleaseId() throws IOException {
    String productId = "test-product";
    long ghReleaseId = 0L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result, "Link should not be null for zero release ID");
    assertEquals("self", result.getRel().value(), "Link should have 'self' relation type for zero release ID");
    String href = result.getHref();
    assertNotNull(href, "Link href should not be null for zero release ID");
    assertTrue(href.contains(productId), "Link href should contain the product ID: " + productId);
    assertTrue(href.contains("0"), "Link href should contain zero release ID");
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModelWithLargeReleaseId() throws IOException {
    String productId = "test-product";
    long ghReleaseId = Long.MAX_VALUE;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result, "Link should not be null for maximum release ID");
    assertEquals("self", result.getRel().value(), "Link should have 'self' relation type for maximum release ID");
    String href = result.getHref();
    assertNotNull(href, "Link href should not be null for maximum release ID");
    assertTrue(href.contains(productId), "Link href should contain the product ID: " + productId);
    assertTrue(href.contains(String.valueOf(Long.MAX_VALUE)), "Link href should contain the maximum release ID: " + Long.MAX_VALUE);
  }

  @Test
  void testCreateSelfLinkForGithubReleaseModelWithEmptyProductId() throws IOException {
    String productId = "";
    long ghReleaseId = 12345L;

    Link result = GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghReleaseId);

    assertNotNull(result, "Link should not be null for empty product ID");
    assertEquals("self", result.getRel().value(), "Link should have 'self' relation type for empty product ID");
    String href = result.getHref();
    assertNotNull(href, "Link href should not be null for empty product ID");
    assertTrue(href.contains(String.valueOf(ghReleaseId)), "Link href should contain the release ID: " + ghReleaseId);
  }
}
