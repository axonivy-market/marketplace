import { DeprecatedProductInfo } from './deprecated-product-info';

export interface DeprecatedResponse {
  productIds?: string[];
  productDeprecations?: DeprecatedProductInfo[];
  pullRequestUrl: string | null;
}
