export interface DeprecatedProductInfo {
  id: string;
  deprecationDate?: string | null;
  deprecationRequester: string | null;
  isArchivedGithubRepo?: boolean;
}

