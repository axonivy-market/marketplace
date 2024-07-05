export interface ArchivedArtifact {
  lastVersion: string;
  groupId: string;
  artifactId: string;
}

export interface MavenArtifact {
  repoUrl?: string;
  key?: string;
  name: string;
  groupId: string;
  artifactId: string;
  archivedArtifacts?: ArchivedArtifact[];
  type?: string;
  doc?: boolean;
}
