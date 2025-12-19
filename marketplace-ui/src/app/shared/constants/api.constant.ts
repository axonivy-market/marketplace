import { InjectionToken } from '@angular/core';

const API = 'api';
const SYNC = 'sync';

export const API_URI = {
  APP: '/',
  PRODUCT: `${API}/product`,
  PRODUCT_DETAILS: `${API}/product-details`,
  EXTERNAL_DOCUMENT: `${API}/externaldocument`,
  FEEDBACK: `${API}/feedback`,
  FEEDBACK_APPROVAL: `${API}/feedback/approval`,
  PRODUCT_MARKETPLACE_DATA: `${API}/product-marketplace-data`,
  PREVIEW_RELEASE: `${API}/release-preview`,
  MONITOR_DASHBOARD: `${API}/monitor-dashboard/repos`,
  GITHUB_REPORT: `${API}/monitor-dashboard`,
  SYNC_GITHUB_MONITOR: `${API}/monitor-dashboard/${SYNC}`,
  SYNC_TASK_EXECUTION: `${API}/sync-task-execution`,
  SECURITY_MONITOR: `${API}/security-monitor`
};

export const API_PUBLIC_URL = new InjectionToken<any>('ApiPublicUrl');
export const API_INTERNAL_URL = new InjectionToken<any>('ApiInternalUrl');