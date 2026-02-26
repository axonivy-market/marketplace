package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.github.util.GitHubUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.PagedIterable;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.Link;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

  @Test
  void testGetGHCommitDateWhenIOExceptionThrownShouldReturnZero() throws Exception {
    GHCommit commit = Mockito.mock(GHCommit.class);

    when(commit.getCommitDate()).thenThrow(new IOException("IO failure"));

    long result = GitHubUtils.getGHCommitDate(commit);

    Assertions.assertEquals(0L, result,
        "Should return 0 when IOException occurs while getting commit date");
  }

  @Test
  void testGetGHCommitDateWhenCommitIsNullShouldReturnZero() {
    long result = GitHubUtils.getGHCommitDate(null);

    Assertions.assertEquals(0L, result,
        "Should return 0 when commit is null");
  }

  @Test
  void testGetDownloadUrlWhenValidShouldReturnUrl() throws Exception {
    GHContent content = Mockito.mock(GHContent.class);
    String expectedUrl = "https://example.com/file.png";

    Mockito.when(content.getDownloadUrl()).thenReturn(expectedUrl);

    String result = GitHubUtils.getDownloadUrl(content);

    Assertions.assertEquals(expectedUrl, result,
        "Should return the download URL when no exception occurs");
  }

  @Test
  void testGetDownloadUrlWhenIOExceptionThrownShouldReturnEmptyString() throws Exception {
    GHContent content = Mockito.mock(GHContent.class);

    Mockito.when(content.getDownloadUrl())
        .thenThrow(new IOException("IO failure"));

    String result = GitHubUtils.getDownloadUrl(content);

    Assertions.assertEquals(StringUtils.EMPTY, result,
        "Should return empty string when IOException occurs");
  }

  @Test
  void testMapPagedIteratorToListWhenValidShouldReturnList() throws Exception {
    PagedIterable<String> paged = Mockito.mock(PagedIterable.class);

    List<String> expected = List.of("A", "B");
    Mockito.when(paged.toList()).thenReturn(expected);

    List<String> result = GitHubUtils.mapPagedIteratorToList(paged);

    Assertions.assertEquals(expected, result,
        "Should return list when paged.toList() succeeds");
  }

  @Test
  void testMapPagedIteratorToListWhenIOExceptionThrownShouldReturnEmptyList() throws Exception {
    PagedIterable<String> paged = Mockito.mock(PagedIterable.class);

    Mockito.when(paged.toList())
        .thenThrow(new IOException("IO failure"));

    List<String> result = GitHubUtils.mapPagedIteratorToList(paged);

    Assertions.assertTrue(result.isEmpty(),
        "Should return empty list when IOException occurs");
  }

  @Test
  void testMapPagedIteratorToListWhenPagedIsNullShouldReturnEmptyList() {
    List<String> result = GitHubUtils.mapPagedIteratorToList(null);

    Assertions.assertTrue(result.isEmpty(),
        "Should return empty list when paged is null");
  }

  @Test
  void testFindImagesInDirectoryWhenIOExceptionThrownShouldNotAddImages() throws Exception {
    GHContent directory = Mockito.mock(GHContent.class);
    List<GHContent> images = new ArrayList<>();

    Mockito.when(directory.isDirectory()).thenReturn(true);
    Mockito.when(directory.listDirectoryContent())
        .thenThrow(new IOException("IO failure"));

    List<GHContent> files = List.of(directory);
    GitHubUtils.findImages(files, images);

    Assertions.assertTrue(images.isEmpty(),
        "Images list should remain empty when IOException occurs");
  }

  @Test
  void testExtractedContentStreamWhenIOExceptionThrownShouldReturnNull() throws Exception {
    GHContent content = Mockito.mock(GHContent.class);

    Mockito.when(content.read())
        .thenThrow(new IOException("IO failure"));

    InputStream result = GitHubUtils.extractedContentStream(content);

    Assertions.assertNull(result,
        "Should return null when IOException occurs while reading content");
  }
}
