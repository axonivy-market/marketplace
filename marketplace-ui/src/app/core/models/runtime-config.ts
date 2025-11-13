import { makeStateKey, StateKey } from '@angular/core';

export interface RuntimeConfig {
  apiUrl: string;
  githubOAuthAppClientId: string;
  githubOAuthCallback: string;
  githubApiUrl: string;
  dayInMiliseconds: number;
  matomoSiteId: number;
  matomoTrackerUrl: string;
}

export const ENV_VAR_NAMES = {
  MARKET_API_URL: 'MARKET_API_URL',
  MARKET_GITHUB_OAUTH_APP_CLIENT_ID: 'MARKET_GITHUB_OAUTH_APP_CLIENT_ID',
  MARKET_GITHUB_OAUTH_CALLBACK: 'MARKET_GITHUB_OAUTH_CALLBACK',
  MARKET_GITHUB_API_URL: 'MARKET_GITHUB_API_URL',
  MARKET_DAY_IN_MILLISECONDS: 'MARKET_DAY_IN_MILLISECONDS',
  MARKET_MATOMO_SITE_ID: 'MARKET_MATOMO_SITE_ID',
  MARKET_MATOMO_TRACKER_URL: 'MARKET_MATOMO_TRACKER_URL'
} as const;

export const RUNTIME_CONFIG_KEYS = {
  MARKET_API_URL: 'apiUrl',
  MARKET_GITHUB_OAUTH_APP_CLIENT_ID: 'githubOAuthAppClientId',
  MARKET_GITHUB_OAUTH_CALLBACK: 'githubOAuthCallback',
  MARKET_GITHUB_API_URL: 'githubApiUrl',
  MARKET_DAY_IN_MILLISECONDS: 'dayInMiliseconds',
  MARKET_MATOMO_SITE_ID: 'matomoSiteId',
  MARKET_MATOMO_TRACKER_URL: 'matomoTrackerUrl'
} as const satisfies Record<string, keyof RuntimeConfig>;

// TransferState Key for Runtime Configuration
export const RUNTIME_CONFIG_KEY: StateKey<RuntimeConfig> = makeStateKey<RuntimeConfig>('runtime-config');
