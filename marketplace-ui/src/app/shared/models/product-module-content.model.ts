import { DisplayValue } from "./display-value.model";

export interface ProductModuleContent {
  tag: string;
  description: DisplayValue | null;
  demo: string;
  setup: string;
  isDependency: boolean;
  name: string;
  groupId: string;
  artifactId: string;
  type: string;
}
