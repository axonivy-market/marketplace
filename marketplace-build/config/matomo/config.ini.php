; <?php exit; ?> DO NOT REMOVE THIS LINE
; file automatically generated or modified by Matomo; you can manually override the default values in global.ini.php by redefining them in this file.
[database]
host = "mariadb"
username = "xxxx"
password = "xxxx"
dbname = "matomo_market"
tables_prefix = "mp_"
charset = "utf8mb4"
collation = "utf8mb4_0900_ai_ci"
schema = Mariadb

[General]
assume_secure_protocol = 1
proxy_uri_header = 1
enable_trusted_host_check = 0

; Add these lines to fix GeoIP2 encoding issues
force_ssl = 1
charset = "utf-8"

proxy_client_headers[] = HTTP_X_REAL_IP
proxy_client_headers[] = HTTP_X_FORWARDED_FOR
proxy_client_headers[] = HTTP_CF_CONNECTING_IP
proxy_client_headers[] = HTTP_CLIENT_IP
proxy_host_headers[] = HTTP_X_FORWARDED_HOST

salt = "xxxx"
trusted_hosts[] = "market.axonivy.com"
session_save_handler = "dbtable"

[PluginsInstalled]
PluginsInstalled[] = "Diagnostics"
PluginsInstalled[] = "Login"
PluginsInstalled[] = "CoreAdminHome"
PluginsInstalled[] = "UsersManager"
PluginsInstalled[] = "SitesManager"
PluginsInstalled[] = "Installation"
PluginsInstalled[] = "Monolog"
PluginsInstalled[] = "Intl"
PluginsInstalled[] = "JsTrackerInstallCheck"
PluginsInstalled[] = "CoreVue"
PluginsInstalled[] = "CorePluginsAdmin"
PluginsInstalled[] = "CoreHome"
PluginsInstalled[] = "WebsiteMeasurable"
PluginsInstalled[] = "IntranetMeasurable"
PluginsInstalled[] = "CoreVisualizations"
PluginsInstalled[] = "Proxy"
PluginsInstalled[] = "API"
PluginsInstalled[] = "Widgetize"
PluginsInstalled[] = "Transitions"
PluginsInstalled[] = "LanguagesManager"
PluginsInstalled[] = "Actions"
PluginsInstalled[] = "Dashboard"
PluginsInstalled[] = "MultiSites"
PluginsInstalled[] = "Referrers"
PluginsInstalled[] = "UserLanguage"
PluginsInstalled[] = "DevicesDetection"
PluginsInstalled[] = "Goals"
PluginsInstalled[] = "Ecommerce"
PluginsInstalled[] = "SEO"
PluginsInstalled[] = "Events"
PluginsInstalled[] = "UserCountry"
PluginsInstalled[] = "GeoIp2"
PluginsInstalled[] = "VisitsSummary"
PluginsInstalled[] = "VisitFrequency"
PluginsInstalled[] = "VisitTime"
PluginsInstalled[] = "VisitorInterest"
PluginsInstalled[] = "RssWidget"
PluginsInstalled[] = "Feedback"
PluginsInstalled[] = "TwoFactorAuth"
PluginsInstalled[] = "CoreUpdater"
PluginsInstalled[] = "CoreConsole"
PluginsInstalled[] = "ScheduledReports"
PluginsInstalled[] = "UserCountryMap"
PluginsInstalled[] = "Live"
PluginsInstalled[] = "PrivacyManager"
PluginsInstalled[] = "ImageGraph"
PluginsInstalled[] = "Annotations"
PluginsInstalled[] = "MobileMessaging"
PluginsInstalled[] = "Overlay"
PluginsInstalled[] = "SegmentEditor"
PluginsInstalled[] = "Insights"
PluginsInstalled[] = "Morpheus"
PluginsInstalled[] = "Contents"
PluginsInstalled[] = "BulkTracking"
PluginsInstalled[] = "Resolution"
PluginsInstalled[] = "DevicePlugins"
PluginsInstalled[] = "Heartbeat"
PluginsInstalled[] = "Marketplace"
PluginsInstalled[] = "ProfessionalServices"
PluginsInstalled[] = "UserId"
PluginsInstalled[] = "CustomJsTracker"
PluginsInstalled[] = "Tour"
PluginsInstalled[] = "PagePerformance"
PluginsInstalled[] = "CustomDimensions"
PluginsInstalled[] = "FeatureFlags"
