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
    numberOfSecretScanningAlerts: number;
  };
  branchProtectionEnabled: boolean;
  lastCommitSHA: string;
  lastCommitDate: string;
}
