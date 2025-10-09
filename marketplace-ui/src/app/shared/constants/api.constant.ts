import { InjectionToken } from '@angular/core';

const API = 'api';

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
  CHATBOT: `${API}/chat/message`
};

export const API_PUBLIC_URL = new InjectionToken<any>('ApiPublicUrl');
export const API_INTERNAL_URL = new InjectionToken<any>('ApiInternalUrl');