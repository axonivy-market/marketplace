package com.axonivy.market.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppSettingKey {
// =========================
  // SCHEDULING
  // =========================

  PRODUCTS_CRON(
      "market.scheduling.products-cron",
      "0 0 */6 * * *",
      "SCHEDULING",
      "Cron expression for product synchronization.",
      false),

  PRODUCTS_DEPENDENCY_CRON(
      "market.scheduling.products-dependency-cron",
      "0 30 */6 * * *",
      "SCHEDULING",
      "Cron expression for dependency synchronization.",
      false),

  GITHUB_REPOS_CRON(
      "market.scheduling.github-repos-cron",
      "0 0 */12 * * *",
      "SCHEDULING",
      "Cron expression for GitHub repository synchronization.",
      false),

  DOCUMENTS_CRON(
      "market.scheduling.documents-cron",
      "0 15 */12 * * *",
      "SCHEDULING",
      "Cron expression for documentation synchronization.",
      false),

  PRODUCT_RELEASE_NOTES_CRON(
      "market.scheduling.products-release-notes-cron",
      "0 0 2 * * *",
      "SCHEDULING",
      "Cron expression for release notes synchronization.",
      false),

  SECURITY_MONITOR_CRON(
      "market.scheduling.security-monitor-cron",
      "0 */30 * * * *",
      "SCHEDULING",
      "Cron expression for security monitoring.",
      false),

  SEND_NOTIFICATION_SECURITY_MONITOR_CRON(
      "market.scheduling.send-notification-security-monitor-cron",
      "0 0 9 * * *",
      "SCHEDULING",
      "Cron expression for security monitor notifications.",
      false),

  // =========================
  // GITHUB
  // =========================

  GITHUB_OAUTH_CLIENT_ID(
      "market.github.oauth2-clientId",
      "",
      "GITHUB",
      "GitHub OAuth application client id.",
      false),

  GITHUB_OAUTH_CLIENT_SECRET(
      "market.github.oauth2-clientSecret",
      "",
      "GITHUB",
      "GitHub OAuth application client secret.",
      true),

  GITHUB_TOKEN(
      "market.github.token",
      "",
      "GITHUB",
      "GitHub personal access token used for API requests.",
      true),

  GITHUB_CONNECT_TIMEOUT(
      "market.github.connect-timeout",
      "10000",
      "GITHUB",
      "GitHub API connection timeout in milliseconds.",
      false),

  GITHUB_MARKET_BRANCH(
      "market.github.market.branch",
      "master",
      "GITHUB",
      "Marketplace repository branch used for synchronization.",
      false),

  // =========================
  // MATOMO
  // =========================

  MATOMO_ENABLED(
      "matomo.tracker.enabled",
      "true",
      "MATOMO",
      "Enable Matomo tracking.",
      false),

  MATOMO_API_ENDPOINT(
      "matomo.tracker.api-endpoint",
      "",
      "MATOMO",
      "Matomo tracker API endpoint.",
      false),

  MATOMO_SITE_ID(
      "matomo.tracker.default-site-id",
      "1",
      "MATOMO",
      "Default Matomo site identifier.",
      false),

// =========================
// MAIL
// =========================

  MAIL_HOST(
      "spring.mail.host",
      "",
      "MAIL",
      "SMTP server host.",
      false),

  MAIL_PORT(
      "spring.mail.port",
      "587",
      "MAIL",
      "SMTP server port.",
      false),

  MAIL_USERNAME(
      "spring.mail.username",
      "",
      "MAIL",
      "SMTP username.",
      false),

  MAIL_PASSWORD(
      "spring.mail.password",
      "",
      "MAIL",
      "SMTP password.",
      true),

  MAIL_SMTP_AUTH(
      "spring.mail.properties.mail.smtp.auth",
      "true",
      "MAIL",
      "Enable SMTP authentication.",
      false),

  MAIL_SMTP_STARTTLS_ENABLE(
      "spring.mail.properties.mail.smtp.starttls.enable",
      "true",
      "MAIL",
      "Enable SMTP STARTTLS.",
      false),

  MAIL_FROM(
      "spring.mail.from",
      "",
      "MAIL",
      "Default sender email address.",
      false),

  MAIL_TO(
      "spring.mail.to",
      "",
      "MAIL",
      "Default recipient email address.",
      false),

  // =========================
  // SECURITY
  // =========================

  LIMITED_REQUEST_PATHS(
      "market.limited.request-paths",
      "",
      "SECURITY",
      "Restricted request paths.",
      false),

  CLICK_CAPACITY(
      "market.allowed.click-capacity",
      "100",
      "SECURITY",
      "Maximum allowed click capacity.",
      false),

  // =========================
  // CORS
  // =========================

  CORS_ALLOWED_ORIGIN_PATTERNS(
      "market.cors.allowed.origin.patterns",
      "*",
      "CORS",
      "Allowed CORS origin patterns.",
      false),

  // =========================
  // APPLICATION
  // =========================

  APPLICATION_TITLE(
      "market.info.title",
      "Marketplace app",
      "APPLICATION",
      "Application title.",
      false),

  APPLICATION_DESCRIPTION(
      "market.info.description",
      "The restful api for marketplace website",
      "APPLICATION",
      "Application description.",
      false),

  AXON_IVY_DEVELOPER_URL(
    "axon.ivy.developer.url",
        "https://developer.axonivy.com",
        "GENERAL",
        "Axon Ivy Developer Portal URL.",
        false);

  private final String key;
  private final String defaultValue;
  private final String category;
  private final String description;
  private final boolean encrypted;
}