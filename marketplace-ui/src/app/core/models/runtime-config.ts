import { makeStateKey, StateKey } from '@angular/core';

export interface RuntimeConfig {
  apiUrl: string;
  githubClientId: string;
  githubAuthCallbackPath: string;
  githubApiUrl: string;
  dayInMiliseconds: number;
  matomoSiteId: number;
  matomoTrackerUrl: string;
}

export const ENV_VAR_NAMES = {
  API_URL: 'MARKET_API_URL',
  GITHUB_CLIENT_ID: 'MARKET_GITHUB_CLIENT_ID',
  GITHUB_AUTH_CALLBACK_PATH: 'MARKET_GITHUB_AUTH_CALLBACK_PATH',
  GITHUB_API_URL: 'MARKET_GITHUB_API_URL',
  DAY_IN_MILLISECONDS: 'MARKET_DAY_IN_MILLISECONDS',
  MATOMO_SITE_ID: 'MARKET_MATOMO_SITE_ID',
  MATOMO_TRACKER_URL: 'MARKET_MATOMO_TRACKER_URL'
} as const;

export const RUNTIME_CONFIG_KEYS = {
  API_URL: 'apiUrl',
  GITHUB_CLIENT_ID: 'githubClientId',
  GITHUB_AUTH_CALLBACK_PATH: 'githubAuthCallbackPath',
  GITHUB_API_URL: 'githubApiUrl',
  DAY_IN_MILLISECONDS: 'dayInMiliseconds',
  MATOMO_SITE_ID: 'matomoSiteId',
  MATOMO_TRACKER_URL: 'matomoTrackerUrl'
} as const satisfies Record<string, keyof RuntimeConfig>;

// TransferState Key for Runtime Configuration
export const RUNTIME_CONFIG_KEY: StateKey<RuntimeConfig> = makeStateKey<RuntimeConfig>('runtime-config');
