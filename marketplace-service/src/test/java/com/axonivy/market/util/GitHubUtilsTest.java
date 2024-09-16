package com.axonivy.market.util;

import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.github.util.GitHubUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;
import static com.axonivy.market.constants.MetaConstants.META_FILE;

@ExtendWith(MockitoExtension.class)
class GitHubUtilsTest {
  private static final String JIRA_CONNECTOR = "Jira Connector";

  @Test
  void testConvertArtifactIdToName() {
    String defaultArtifactId = "adobe-acrobat-sign-connector";
    String result = GitHubUtils.convertArtifactIdToName(defaultArtifactId);
    Assertions.assertEquals("Adobe Acrobat Sign Connector", result);

    result = GitHubUtils.convertArtifactIdToName(null);
    Assertions.assertEquals(StringUtils.EMPTY, result);

    result = GitHubUtils.convertArtifactIdToName(StringUtils.EMPTY);
    Assertions.assertEquals(StringUtils.EMPTY, result);

    result = GitHubUtils.convertArtifactIdToName(" ");
    Assertions.assertEquals(StringUtils.EMPTY, result);
  }

  @Test
  void testBuildProductJsonFilePath() {
    String result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.PORTAL.getId());
    Assertions.assertEquals("AxonIvyPortal/portal-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.CONNECTIVITY_FEATURE.getId());
    Assertions.assertEquals("connectivity/connectivity-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.ERROR_HANDLING.getId());
    Assertions.assertEquals("error-handling/error-handling-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.WORKFLOW_DEMO.getId());
    Assertions.assertEquals("workflow/workflow-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.MICROSOFT_365.getId());
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-connector", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.MICROSOFT_CALENDAR.getId());
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-calendar", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.MICROSOFT_TEAMS.getId());
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-chat", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.MICROSOFT_MAIL.getId());
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-mail", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.MICROSOFT_TODO.getId());
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-todo", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.HTML_DIALOG_DEMO.getId());
    Assertions.assertEquals("html-dialog/html-dialog-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.RULE_ENGINE_DEMOS.getId());
    Assertions.assertEquals("rule-engine/rule-engine-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.OPENAI_CONNECTOR.getId());
    Assertions.assertEquals("openai-connector-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProduct.OPENAI_ASSISTANT.getId());
    Assertions.assertEquals("openai-assistant-product", result);
  }

  @Test
  void testGetNonStandardImageFolder() {
    String result = GitHubUtils.getNonStandardImageFolder(NonStandardProduct.EXCEL_IMPORTER.getId());
    Assertions.assertEquals("doc", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProduct.EXPRESS_IMPORTER.getId());
    Assertions.assertEquals("img", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProduct.DEEPL_CONNECTOR.getId());
    Assertions.assertEquals("img", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProduct.GRAPHQL_DEMO.getId());
    Assertions.assertEquals("assets", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProduct.OPENAI_ASSISTANT.getId());
    Assertions.assertEquals("docs", result);

    result = GitHubUtils.getNonStandardImageFolder(JIRA_CONNECTOR);
    Assertions.assertEquals("images", result);
  }

  @Test
  void testSortMetaJsonFirst() {
    int result = GitHubUtils.sortMetaJsonFirst(META_FILE, LOGO_FILE);
    Assertions.assertEquals(-1, result);

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, META_FILE);
    Assertions.assertEquals(1, result);

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, LOGO_FILE);
    Assertions.assertEquals(0, result);
  }
}
