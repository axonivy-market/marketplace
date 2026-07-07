import { InjectionToken } from '@angular/core';

const API = 'api';
const SYNC = 'sync';
const INTERNAL = 'internal';

export const API_URI = {
  APP: '/',
  PRODUCT: `${API}/product`,
  IDS: `${API}/product/ids`,
  PRODUCT_DETAILS: `${API}/product-details`,
  EXTERNAL_DOCUMENT: `${API}/externaldocument`,
  FEEDBACK: `${API}/feedback`,
  FEEDBACK_APPROVAL: `${API}/feedback/approval`,
  PRODUCT_MARKETPLACE_DATA: `${API}/product-marketplace-data`,
  CUSTOM_SORT: `${API}/product-marketplace-data/custom-sort`,
  PREVIEW_RELEASE: `${API}/release-preview`,
  MONITOR_DASHBOARD: `${API}/monitor-dashboard/repos`,
  GITHUB_REPORT: `${API}/monitor-dashboard`,
  SYNC_GITHUB_MONITOR: `${API}/monitor-dashboard/${SYNC}`,
  SYNC_SECURITY_MONITOR: `${API}/${INTERNAL}/security-monitor`,
  SYNC_TASK_EXECUTION: `${API}/${INTERNAL}/sync-task-execution`,
  SECURITY_MONITOR: `${API}/${INTERNAL}/security-monitor`,
  GITHUB_REQUEST_ACCESS: 'auth/github/request-access',
  GITHUB_VALIDATE_TOKEN: 'auth/github/validate-token',
  GITHUB_CALLBACK: 'auth/github/callback',
  ADMIN_GITHUB_AUTHORIZATION: 'auth/admin/github/authorization',
  ADMIN_GITHUB_CALLBACK: 'auth/admin/github/callback',
  ADMIN_SESSION: 'auth/admin/session',
  ADMIN_CSRF: 'auth/admin/csrf',
  ADMIN_LOGOUT: 'auth/admin/logout',
  LOGS: `${API}/${INTERNAL}/logs`,
  GITHUB_LOGIN: 'auth/github/login',
  RELEASE_LETTERS: `${API}/release-letters`,
  LATEST_RELEASE_LETTERS: `${API}/release-letters/latest`,
  PRODUCT_DEPRECATIONS: `${API}/product-marketplace-data/deprecations`,
  APP_SETTINGS: `${API}/${INTERNAL}/settings`,
  PRODUCT_MARKETPLACE_DATA_DEPRECATED_BY_ID: (id: string) =>
    `${API}/product-marketplace-data/${encodeURIComponent(id)}/deprecations`,
  PRODUCT_MARKETPLACE_DATA_ARCHIVE_BY_ID: (id: string) =>
    `${API}/product-marketplace-data/${encodeURIComponent(id)}/archive`
};

export const API_PUBLIC_URL = new InjectionToken<any>('ApiPublicUrl');
export const API_INTERNAL_URL = new InjectionToken<any>('ApiInternalUrl');
