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
  vendorImage: string;
  vendorImageDarkMode: string;
  platformReview: string;
  newestReleaseVersion: string;
  cost: string;
  sourceUrl: string;
  statusBadgeUrl: string;
  language: string;
  industry: string;
  contactUs: boolean;
  installationCount: number;
  productModuleContent: ProductModuleContent;
  mavenDropins: boolean;
  metaProductJsonUrl?: string;
  compatibilityRange?: string;
  isFocusedProduct: boolean;
  _links: {
    self: {
      href: string;
    };
  };
}
