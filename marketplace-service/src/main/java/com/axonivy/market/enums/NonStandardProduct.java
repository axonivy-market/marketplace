package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum NonStandardProduct {
  PORTAL ( "portal",true,"images","AxonIvyPortal/portal-product" ),
  MICROSOFT_REPO_NAME ( "msgraph-connector",false,"images",""),
  MICROSOFT_365 ( "msgraph",false,"images","msgraph-connector-product/products/msgraph-connector"), // No meta.json
  MICROSOFT_CALENDAR ( "msgraph-calendar",false,"images","msgraph-connector-product/products/msgraph-calendar"), // no fix product json
  MICROSOFT_MAIL ( "msgraph-mail",false,"images","msgraph-connector-product/products/msgraph-mail"),// no fix product json
  MICROSOFT_TEAMS ( "msgraph-chat",false,"images","msgraph-connector-product/products/msgraph-chat"),// no fix product json
  MICROSOFT_TODO ( "msgraph-todo",false,"images","msgraph-connector-product/products/msgraph-todo"),// no fix product json
  CONNECTIVITY_FEATURE ( "connectivity-demo",false,"images","connectivity/connectivity-demos-product"),
  EMPLOYEE_ONBOARDING ( "employee-onboarding",false,"images",""), // Invalid meta.json
  ERROR_HANDLING ( "error-handling-demo",false,"images","error-handling/error-handling-demos-product"),
  RULE_ENGINE_DEMOS ( "rule-engine-demo",false,"images","rule-engine/rule-engine-demos-product"),
  WORKFLOW_DEMO ( "workflow-demo",false,"images","workflow/workflow-demos-product"),
  HTML_DIALOG_DEMO ( "html-dialog-demo",false,"images","html-dialog/html-dialog-demos-product"),
  PROCESSING_VALVE_DEMO ( "processing-valve-demo",false,"images",""),// no product json
  OPENAI_CONNECTOR ( "openai-connector",false,"images","openai-connector-product"),
  OPENAI_ASSISTANT ( "openai-assistant",false,"docs","openai-assistant-product"),
  // Non standard image folder name
  EXCEL_IMPORTER ( "excel-importer",false,"doc",""),
  EXPRESS_IMPORTER ( "express-importer",false,"img",""),
  GRAPHQL_DEMO ( "graphql-demo",false,"assets",""),
  DEEPL_CONNECTOR ( "deepl-connector",false,"img",""),
  DEFAULT("",false,"images","");

  private final String id;
  private final boolean isVersionTagNumberOnly;
  private final String pathToImageFolder;
  private final String pathToProductFolder;

  public static NonStandardProduct findById(String id){
    return Arrays.stream(NonStandardProduct.values()).filter(product -> id.equalsIgnoreCase(product.getId())).findAny().orElse(DEFAULT);
  }
}
