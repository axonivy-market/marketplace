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
  
    // Define time units and their labels
    const timeUnits = [
      { seconds: 60, singular: 'minute', plural: 'minutes' },
      { seconds: 3600, singular: 'hour', plural: 'hours' },
      { seconds: 86400, singular: 'day', plural: 'days' },
      { seconds: 604800, singular: 'week', plural: 'weeks' },
      { seconds: 2592000, singular: 'month', plural: 'months' },
      { seconds: 31536000, singular: 'year', plural: 'years' },
    ];
  
    if (diffInSeconds < 60) {
      return 'just now';
    }
  
    // Iterate through time units
    for (let i = 0; i < timeUnits.length; i++) {
      const { seconds, singular, plural } = timeUnits[i];
      if (diffInSeconds < seconds) {
        const value = Math.floor(diffInSeconds / (seconds / 60));
        return `${value} ${value === 1 ? singular : plural} ago`;
      }
    }
  
    // Handle years (last time unit in the array)
    const { singular, plural } = timeUnits[timeUnits.length - 1];
    const years = Math.floor(diffInSeconds / timeUnits[timeUnits.length - 1].seconds);
    return `${years} ${years === 1 ? singular : plural} ago`;
  }
}

type RepoPage = 
  | 'security' 
  | 'dependabot' 
  | 'codeScanning' 
  | 'secretScanning' 
  | 'branches' 
  | 'lastCommit';
