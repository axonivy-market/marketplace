export interface ProductSecurityInfo {
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
  secretScanning: {
    status: string;
    numberOfAlerts: number;
  };
  branchProtectionEnabled: boolean;
  lastCommitSHA: string;
  lastCommitDate: Date;
}