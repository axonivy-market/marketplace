import { DeprecatedProductInfo } from './deprecated-product-info';

export interface DeprecationResponse {
  productDeprecations?: DeprecatedProductInfo[];
  pullRequestUrl: string | null;
}
