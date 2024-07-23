import { DisplayValue } from './display-value.model';
import { ProductModuleContent } from './product-module-content.model';

export interface ProductDetail {
  id: string;
  names: DisplayValue;
  shortDescriptions: DisplayValue;
  logoUrl: string;
  type: string;
  tags: string[];
  vendor: string;
  vendorUrl: string;
  platformReview: string;
  newestReleaseVersion: string;
  cost: string;
  sourceUrl: string;
  statusBadgeUrl: string;
  language: string;
  industry: string;
  compatibility: string;
  contactUs: boolean;
  installationCount: number;
  productModuleContent: ProductModuleContent;
  _links: {
    self: {
      href: string;
    };
  };
}
