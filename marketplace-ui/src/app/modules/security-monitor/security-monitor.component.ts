import { Component, inject, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SecurityMonitorService } from './security-monitor.service';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { CommonModule } from '@angular/common';
import { TimeAgoPipe } from '../../shared/pipes/time-ago.pipe';
import { GITHUB_MARKET_ORG_URL, UNAUTHORIZED } from '../../shared/constants/common.constant';

@Component({
  selector: 'app-security-monitor',
  standalone: true,
  imports: [CommonModule, FormsModule, TimeAgoPipe],
  templateUrl: './security-monitor.component.html',
  styleUrls: ['./security-monitor.component.scss'],
  encapsulation: ViewEncapsulation.Emulated,
})
export class SecurityMonitorComponent {
  isAuthenticated = false;
  token = '';
  errorMessage = '';
  repos: ProductSecurityInfo[] = [];

  private securityMonitorService = inject(SecurityMonitorService);

  ngOnInit(): void {
    try {
      const sessionData = sessionStorage.getItem('security-monitor-data');
      if (sessionData) {
        this.repos = JSON.parse(sessionData) as ProductSecurityInfo[];
        this.isAuthenticated = true;
      }
    } catch (error) {
      console.error('Failed to parse session data:', error);
      sessionStorage.removeItem('security-monitor-data');
      this.repos = [];
      this.isAuthenticated = false;
    }
  }

  onSubmit(): void {
    this.token = this.token ?? sessionStorage.getItem('security-monitor-token') ?? '';
    if (!this.token) {
      this.errorMessage = 'Token is required';
      this.isAuthenticated = false;
      sessionStorage.removeItem('security-monitor-token');
      sessionStorage.removeItem('security-monitor-data');
      return;
    }

    this.errorMessage = '';

    this.securityMonitorService.getSecurityDetails(this.token).subscribe({
      next: (data) => this.handleSuccess(data),
      error: (err) => this.handleError(err),
    });
  }

  private handleSuccess(data: ProductSecurityInfo[]): void {
    this.repos = data;
    this.isAuthenticated = true;
    sessionStorage.setItem('security-monitor-token', this.token);
    sessionStorage.setItem('security-monitor-data', JSON.stringify(data));
  }

  private handleError(err: any): void {
    this.errorMessage =
      err.status === UNAUTHORIZED
        ? 'Unauthorized access.'
        : 'Failed to fetch security data. Check logs for details.';
    console.error(err);
    this.isAuthenticated = false;
    sessionStorage.removeItem('security-monitor-token');
    sessionStorage.removeItem('security-monitor-data');
  }

  hasAlerts(alerts: Record<string, number>): boolean {
    return Object.keys(alerts).length > 0;
  }

  alertKeys(alerts: Record<string, number>): string[] {
    return Object.keys(alerts);
  }

  navigateToPage(repoName: string, path: string, additionalPath: string = ''): void {
    const url = `${GITHUB_MARKET_ORG_URL}/${repoName}${path}${additionalPath}`;
    window.open(url, '_blank');
  }

  navigateToRepoSecurityPage(repoName: string): void {
    this.navigateToPage(repoName, '/security');
  }

  navigateToRepoDependabotPage(repoName: string): void {
    this.navigateToPage(repoName, '/security/dependabot');
  }

  navigateToRepoCodeScanningPage(repoName: string): void {
    this.navigateToPage(repoName, '/security/code-scanning');
  }

  navigateToRepoSecurityScanningPage(repoName: string): void {
    this.navigateToPage(repoName, '/security/security-scanning');
  }

  navigateToRepoLastCommitPage(repoName: string, lastCommitSHA: string): void {
    this.navigateToPage(repoName, '/commit/', lastCommitSHA);
  }

  navigateToRepoBranchesSettingPage(repoName: string): void {
    this.navigateToPage(repoName, '/settings/branches');
  }
}
