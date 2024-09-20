package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.GitHubConstants.COMMON_IMAGES_FOLDER_NAME;

@Getter
@AllArgsConstructor
public enum NonStandardProduct {
  PORTAL("portal", true, COMMON_IMAGES_FOLDER_NAME, "AxonIvyPortal/portal-product"),
  MICROSOFT_REPO_NAME("msgraph-connector", false, COMMON_IMAGES_FOLDER_NAME, ""),
  MICROSOFT_365("msgraph", false, COMMON_IMAGES_FOLDER_NAME, "msgraph-connector-product/products/msgraph-connector"), // No meta.json
  MICROSOFT_CALENDAR("msgraph-calendar", false, COMMON_IMAGES_FOLDER_NAME, "msgraph-connector-product/products/msgraph-calendar"), // no fix product json
  MICROSOFT_MAIL("msgraph-mail", false, COMMON_IMAGES_FOLDER_NAME, "msgraph-connector-product/products/msgraph-mail"),// no fix product json
  MICROSOFT_TEAMS("msgraph-chat", false, COMMON_IMAGES_FOLDER_NAME, "msgraph-connector-product/products/msgraph-chat"),// no fix product json
  MICROSOFT_TODO("msgraph-todo", false, COMMON_IMAGES_FOLDER_NAME, "msgraph-connector-product/products/msgraph-todo"),// no fix product json
  CONNECTIVITY_FEATURE("connectivity-demo", false, COMMON_IMAGES_FOLDER_NAME, "connectivity/connectivity-demos-product"),
  EMPLOYEE_ONBOARDING("employee-onboarding", false, COMMON_IMAGES_FOLDER_NAME, ""), // Invalid meta.json
  ERROR_HANDLING("error-handling-demo", false, COMMON_IMAGES_FOLDER_NAME, "error-handling/error-handling-demos-product"),
  RULE_ENGINE_DEMOS("rule-engine-demo", false, COMMON_IMAGES_FOLDER_NAME, "rule-engine/rule-engine-demos-product"),
  WORKFLOW_DEMO("workflow-demo", false, COMMON_IMAGES_FOLDER_NAME, "workflow/workflow-demos-product"),
  HTML_DIALOG_DEMO("html-dialog-demo", false, COMMON_IMAGES_FOLDER_NAME, "html-dialog/html-dialog-demos-product"),
  PROCESSING_VALVE_DEMO("processing-valve-demo", false, COMMON_IMAGES_FOLDER_NAME, ""),// no product json
  OPENAI_CONNECTOR("openai-connector", false, COMMON_IMAGES_FOLDER_NAME, "openai-connector-product"),
  OPENAI_ASSISTANT("openai-assistant", false, "docs", "openai-assistant-product"),
  // Non standard image folder name
  EXCEL_IMPORTER("excel-importer", false, "doc", ""),
  EXPRESS_IMPORTER("express-importer", false, "img", ""),
  GRAPHQL_DEMO("graphql-demo", false, "assets", ""),
  DEEPL_CONNECTOR("deepl-connector", false, "img", ""),
  DEFAULT("", false, COMMON_IMAGES_FOLDER_NAME, "");

  private final String id;
  private final boolean isVersionTagNumberOnly;
  private final String pathToImageFolder;
  private final String pathToProductFolder;
  private static final Map<String, NonStandardProduct> NON_STANDARD_PRODUCT_MAP;

  static {
    NON_STANDARD_PRODUCT_MAP = Arrays.stream(NonStandardProduct.values()).collect(Collectors.toMap(NonStandardProduct::getId, Function.identity()));
  }

  public static NonStandardProduct findById(String id) {
    return NON_STANDARD_PRODUCT_MAP.getOrDefault(id, DEFAULT);
  }
}
