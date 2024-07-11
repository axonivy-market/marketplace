package com.axonivy.market.util;

import com.axonivy.market.constants.NonStandardProductPackageConstants;
import com.axonivy.market.github.util.GitHubUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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
    String result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.PORTAL);
    Assertions.assertEquals("AxonIvyPortal/portal-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.CONNECTIVITY_FEATURE);
    Assertions.assertEquals("connectivity/connectivity-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.ERROR_HANDLING);
    Assertions.assertEquals("error-handling/error-handling-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.WORKFLOW_DEMO);
    Assertions.assertEquals("workflow/workflow-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.MICROSOFT_365);
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-connector", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.MICROSOFT_CALENDAR);
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-calendar", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.MICROSOFT_TEAMS);
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-chat", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.MICROSOFT_MAIL);
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-mail", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.MICROSOFT_TODO);
    Assertions.assertEquals("msgraph-connector-product/products/msgraph-todo", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.HTML_DIALOG_DEMO);
    Assertions.assertEquals("html-dialog/html-dialog-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.RULE_ENGINE_DEMOS);
    Assertions.assertEquals("rule-engine/rule-engine-demos-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.OPENAI_CONNECTOR);
    Assertions.assertEquals("openai-connector-product", result);

    result = GitHubUtils.getNonStandardProductFilePath(NonStandardProductPackageConstants.OPENAI_ASSISTANT);
    Assertions.assertEquals("openai-assistant-product", result);
  }

  @Test
  void testGetNonStandardImageFolder() {
    String result = GitHubUtils.getNonStandardImageFolder(NonStandardProductPackageConstants.EXCEL_IMPORTER);
    Assertions.assertEquals("doc", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProductPackageConstants.EXPRESS_IMPORTER);
    Assertions.assertEquals("img", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProductPackageConstants.DEEPL_CONNECTOR);
    Assertions.assertEquals("img", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProductPackageConstants.GRAPHQL_DEMO);
    Assertions.assertEquals("assets", result);

    result = GitHubUtils.getNonStandardImageFolder(NonStandardProductPackageConstants.OPENAI_ASSISTANT);
    Assertions.assertEquals("docs", result);

    result = GitHubUtils.getNonStandardImageFolder(JIRA_CONNECTOR);
    Assertions.assertEquals("images", result);
  }
}
