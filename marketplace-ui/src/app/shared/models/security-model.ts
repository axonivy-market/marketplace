export interface Dependabot {
  alerts: Record<string, any>;
  status: string;
}

export interface CodeScanning {
  alerts: Record<string, any>;
  status: string;
}

export interface SecretsScanning {
  numberOfAlerts: number | null;
  status: string;
}

export interface Repo {
  repoName: string;
  visibility: string;
  branchProtectionEnabled: boolean;
  lastCommitDate: string;
  lastCommitSHA: string;
  dependabot: Dependabot;
  secretsScanning: SecretsScanning;
  codeScanning: CodeScanning;
  archived: boolean;
}