import { DisplayValue } from './display-value.model';
import { MavenArtifact } from './maven-artifact.model';

export interface ExternalDocument {
  productId: string;
  artifactId: string;
  artifactName: string;
  version: string;
  relativeLink: string;
  _links?: {
    self: {
      href: string;
    };
  };
}
