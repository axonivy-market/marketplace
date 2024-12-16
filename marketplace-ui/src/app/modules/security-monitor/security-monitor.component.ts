import { Component, inject, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { SecurityMonitorService } from './security-monitor.service';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { GITHUB_MARKET_ORG_URL, UNAUTHORIZED } from '../../shared/constants/common.constant';

@Component({
  selector: 'app-security-monitor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './security-monitor.component.html',
  styleUrls: ['./security-monitor.component.scss'],
  encapsulation: ViewEncapsulation.Emulated,
})
export class SecurityMonitorComponent {
  isAuthenticated = false;
  token = '';
  errorMessage = '';
  repos: ProductSecurityInfo[] = [];

  private readonly securityMonitorService = inject(SecurityMonitorService);
  private readonly sessionKeys = {
    data: 'security-monitor-data',
    token: 'security-monitor-token',
  };

  ngOnInit(): void {
    this.loadSessionData();
  }

  onSubmit(): void {
    this.token = this.token ?? sessionStorage.getItem(this.sessionKeys.token) ?? '';
    if (!this.token) {
      this.handleMissingToken();
      return;
    }

    this.errorMessage = '';
    this.fetchSecurityDetails();
  }

  private loadSessionData(): void {
    const sessionData = sessionStorage.getItem(this.sessionKeys.data);
    if (sessionData) {
      try {
        this.repos = JSON.parse(sessionData) as ProductSecurityInfo[];
        this.isAuthenticated = true;
      } catch (error) {
        this.clearSessionData();
      }
    }
  }

  private handleMissingToken(): void {
    this.errorMessage = 'Token is required';
    this.isAuthenticated = false;
    this.clearSessionData();
  }

  private fetchSecurityDetails(): void {
    this.securityMonitorService.getSecurityDetails(this.token).subscribe({
      next: data => this.handleSuccess(data),
      error: (err: HttpErrorResponse) => this.handleError(err),
    });
  }

  private handleSuccess(data: ProductSecurityInfo[]): void {
    this.repos = data;
    this.isAuthenticated = true;
    sessionStorage.setItem(this.sessionKeys.token, this.token);
    sessionStorage.setItem(this.sessionKeys.data, JSON.stringify(data));
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED ) {
      this.errorMessage = 'Unauthorized access.';
    }
    else {
      this.errorMessage = 'Failed to fetch security data. Check logs for details.';
    }
    this.isAuthenticated = false;
    this.clearSessionData();
  }

  private clearSessionData(): void {
    sessionStorage.removeItem(this.sessionKeys.token);
    sessionStorage.removeItem(this.sessionKeys.data);
  }

  hasAlerts(alerts: Record<string, number>): boolean {
    return Object.keys(alerts).length > 0;
  }

  alertKeys(alerts: Record<string, number>): string[] {
    return Object.keys(alerts);
  }

  navigateToPage(repoName: string, path: string, additionalPath = ''): void {
    const url = `${GITHUB_MARKET_ORG_URL}/${repoName}${path}${additionalPath}`;
    window.open(url, '_blank');
  }

  navigateToRepoPage(repoName: string, page: RepoPage, lastCommitSHA?: string): void {
    const paths: Record<RepoPage, string> = {
      security: '/security',
      dependabot: '/security/dependabot',
      codeScanning: '/security/code-scanning',
      secretScanning: '/security/secret-scanning',
      branches: '/settings/branches',
      lastCommit: `/commit/${lastCommitSHA ?? ''}`,
    };

    const path = paths[page];
    if (path) {
      this.navigateToPage(repoName, path);
    }
  }

  formatCommitDate(date: string): string {
    const now = new Date().getTime();
    const targetDate = new Date(date).getTime();
    const diffInSeconds = Math.floor((now - targetDate) / 1000);
  
    const formatDuration = (diff: number, unit: number, singular: string, plural: string): string | null => {
      const value = Math.floor(diff / unit);
      if (value < unit) {
        return `${value} ${value === 1 ? singular : plural} ago`;
      }
      return null;
    };
  
    if (diffInSeconds < 60) return 'just now';
  
    return (
      formatDuration(diffInSeconds, 60, 'minute', 'minutes') ??
      formatDuration(diffInSeconds, 3600, 'hour', 'hours') ??
      formatDuration(diffInSeconds, 86400, 'day', 'days') ??
      formatDuration(diffInSeconds, 604800, 'week', 'weeks') ??
      formatDuration(diffInSeconds, 2592000, 'month', 'months') ??
      `${Math.floor(diffInSeconds / 31536000)} year${Math.floor(diffInSeconds / 31536000) === 1 ? '' : 's'} ago`
    );
  }
}

type RepoPage = 
  | 'security' 
  | 'dependabot' 
  | 'codeScanning' 
  | 'secretScanning' 
  | 'branches' 
  | 'lastCommit';
