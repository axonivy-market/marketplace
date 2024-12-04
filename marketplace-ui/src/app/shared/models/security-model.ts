export interface Repo {
  repoName: string;
  visibility: string;
  archived: boolean;
  dependabot: {
    status: string;
    alerts: Record<string, number>;
  };
  codeScanning: {
    status: string;
    alerts: Record<string, number>;
  };
  secretsScanning: {
    status: string;
    numberOfAlerts: number;
  };
  branchProtectionEnabled: boolean;
  lastCommitSHA: string;
  lastCommitDate: string;
}