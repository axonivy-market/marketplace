package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Product JSON constants defining file names and JSON field names used in product.json configuration files and
 * product data parsing.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductJsonConstants {
  public static final String PRODUCT_JSON_FILE = "product.json";
  public static final String LOGO_FILE = "logo.png";
  public static final String LOGO_DARK_FILE = "logo-dark.png";
  public static final String DATA = "data";
  public static final String REPOSITORIES = "repositories";
  public static final String PROJECTS = "projects";
  public static final String ARTIFACT_ID = "artifactId";
  public static final String GROUP_ID = "groupId";
  public static final String TYPE = "type";
  public static final String DEPENDENCIES = "dependencies";
  public static final String INSTALLERS = "installers";
  public static final String MAVEN_IMPORT_INSTALLER_ID = "maven-import";
  public static final String MAVEN_DROPINS_INSTALLER_ID = "maven-dropins";
  public static final String VERSION_VALUE = "${version}";
  public static final String MAVEN_DEPENDENCY_INSTALLER_ID = "maven-dependency";
  public static final String EN_LANGUAGE = "en";
  public static final String DEFAULT_PRODUCT_TYPE = "iar";
}
