export interface ItemDropdown<T extends string = string> {
  activeClass?: string,
  tabId?: string,
  value: T;
  label: string;

  // for Artifact model
  name?: string;
  downloadUrl?: string;
  isProductArtifact?: boolean | null;
  artifactId?: string;
  metaDataJsonUrl?: string;
}
