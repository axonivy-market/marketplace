import { Page } from './page.model';
import { ProductSecurityInfo } from '../product-security-info-model';

export interface SecurityMonitorApiResponse {
  _embedded: {
    productSecurityInfoList: ProductSecurityInfo[];
  };
  page: Page;
}

