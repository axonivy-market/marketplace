import { DisplayValue } from "./display-value.model";

export interface ProductModuleContent {
  tag: string;
  description: DisplayValue | null;
  demo: DisplayValue | null;
  setup: DisplayValue | null;
  isDependency: boolean;
  name: string;
  groupId: string;
  artifactId: string;
  type: string;
  productId: string;
}
