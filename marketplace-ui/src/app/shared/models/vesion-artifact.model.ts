import { ItemDropdown } from './item-dropdown.model';

export interface Artifact {
  name: string;
  downloadUrl: string;
  isProductArtifact: boolean | null;
}

export interface VersionData {
  version: string;
  artifactsByVersion: ItemDropdown[];
}
