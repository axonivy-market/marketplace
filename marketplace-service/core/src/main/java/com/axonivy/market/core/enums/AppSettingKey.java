package com.axonivy.market.core.enums;

import com.axonivy.market.core.constants.SyncTaskConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing application setting keys, their default values, categories, descriptions, and encryption status.
 */
@Getter
@RequiredArgsConstructor
public enum AppSettingKey {

  // =========================
  // SCHEDULING
  // =========================
  PRODUCTS_CRON(
      "market.scheduling.products-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for product synchronization.",
      false),

  PRODUCTS_DEPENDENCY_CRON(
      "market.scheduling.products-dependency-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for dependency synchronization.",
      false),

  GITHUB_REPOS_CRON(
      "market.scheduling.github-repos-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for workflow test status synchronization.",
      false),

  DOCUMENTS_CRON(
      "market.scheduling.documents-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for documentation synchronization.",
      false),

  PRODUCT_RELEASE_NOTES_CRON(
      "market.scheduling.products-release-notes-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for release notes synchronization.",
      false),

  SECURITY_MONITOR_CRON(
      "market.scheduling.security-monitor-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for security monitoring.",
      false),

  SEND_NOTIFICATION_SECURITY_MONITOR_CRON(
      "market.scheduling.send-notification-security-monitor-cron",
      SyncTaskConstants.DEFAULT_SCHEDULE_CRON,
      AppSettingCategory.SCHEDULING.name(),
      "Cron expression for security monitor notifications.",
      false),

  // =========================
  // GITHUB
  // =========================

  GITHUB_OAUTH_CLIENT_ID(
      "market.github.oauth2-clientId",
      "",
      AppSettingCategory.GITHUB.name(),
      "GitHub OAuth application client id.",
      true),

  GITHUB_OAUTH_CLIENT_SECRET(
      "market.github.oauth2-clientSecret",
      "",
      AppSettingCategory.GITHUB.name(),
      "GitHub OAuth application client secret.",
      true),

  GITHUB_TOKEN(
      "market.github.token",
      "",
      AppSettingCategory.GITHUB.name(),
      "GitHub personal access token used for API requests.",
      true),

  GITHUB_CONNECT_TIMEOUT(
      "market.github.connect-timeout",
      "10000",
      AppSettingCategory.GITHUB.name(),
      "GitHub API connection timeout in milliseconds.",
      false),

  GITHUB_MARKET_BRANCH(
      "market.github.market.branch",
      "master",
      AppSettingCategory.GITHUB.name(),
      "Marketplace repository branch used for synchronization.",
      false),

  // =========================
  // MATOMO
  // =========================

  MATOMO_ENABLED(
      "matomo.tracker.enabled",
      "true",
      AppSettingCategory.MATOMO.name(),
      "Enable Matomo app tracking.",
      false),

  MATOMO_API_ENDPOINT(
      "matomo.tracker.api-endpoint",
      "",
      AppSettingCategory.MATOMO.name(),
      "Matomo app tracker API endpoint.",
      false),

  MATOMO_SITE_ID(
      "matomo.tracker.default-site-id",
      "1",
      AppSettingCategory.MATOMO.name(),
      "Default Matomo app site identifier.",
      true),

  MATOMO_STABLE_ENABLED(
      "matomo.stable.tracker.enabled",
      "true",
      AppSettingCategory.MATOMO.name(),
      "Enable Matomo stable tracking.",
      false),

  MATOMO_STABLE_API_ENDPOINT(
      "matomo.stable.tracker.api-endpoint",
      "",
      AppSettingCategory.MATOMO.name(),
      "Matomo stable tracker API endpoint.",
      false),

  MATOMO_STABLE_SITE_ID(
      "matomo.stable.tracker.default-site-id",
      "1",
      AppSettingCategory.MATOMO.name(),
      "Default Matomo stable site identifier.",
      true),

// =========================
// MAIL
// =========================

  MAIL_HOST(
      "spring.mail.host",
      "",
      AppSettingCategory.MAIL.name(),
      "SMTP server host.",
      false),

  MAIL_PORT(
      "spring.mail.port",
      "587",
      AppSettingCategory.MAIL.name(),
      "SMTP server port.",
      false),

  MAIL_USERNAME(
      "spring.mail.username",
      "",
      AppSettingCategory.MAIL.name(),
      "SMTP username.",
      false),

  MAIL_PASSWORD(
      "spring.mail.password",
      "",
      AppSettingCategory.MAIL.name(),
      "SMTP password.",
      true),

  MAIL_SMTP_AUTH(
      "spring.mail.properties.mail.smtp.auth",
      "true",
      AppSettingCategory.MAIL.name(),
      "Enable SMTP authentication.",
      false),

  MAIL_SMTP_STARTTLS_ENABLE(
      "spring.mail.properties.mail.smtp.starttls.enable",
      "true",
      AppSettingCategory.MAIL.name(),
      "Enable SMTP STARTTLS.",
      false),

  MAIL_FROM(
      "spring.mail.from",
      "",
      AppSettingCategory.MAIL.name(),
      "Default sender email address.",
      false),

  MAIL_TO(
      "spring.mail.to",
      "",
      AppSettingCategory.MAIL.name(),
      "Default recipient email address.",
      false),

  // =========================
  // SECURITY
  // =========================

  LIMITED_REQUEST_PATHS(
      "market.limited.request-paths",
      "",
      AppSettingCategory.SECURITY.name(),
      "Restricted request paths.",
      false),

  CLICK_CAPACITY(
      "market.allowed.click-capacity",
      "5",
      AppSettingCategory.SECURITY.name(),
      "Maximum allowed click capacity.",
      false),

  AXON_IVY_DEVELOPER_URL(
      "axon.ivy.developer.url",
      "https://developer.axonivy.com",
      AppSettingCategory.GENERAL.name(),
      "Axon Ivy Developer URL.",
      false);

  private final String key;
  private final String defaultValue;
  private final String category;
  private final String description;
  private final boolean encrypted;
}