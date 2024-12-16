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
  
    const SECONDS_IN_A_MINUTE = 60;
    const SECONDS_IN_AN_HOUR = 60 * SECONDS_IN_A_MINUTE;
    const SECONDS_IN_A_DAY = 24 * SECONDS_IN_AN_HOUR;
    const SECONDS_IN_A_WEEK = 7 * SECONDS_IN_A_DAY;
    const SECONDS_IN_A_MONTH = 30 * SECONDS_IN_A_DAY;
    const SECONDS_IN_A_YEAR = 12 * SECONDS_IN_A_MONTH;
  
    const formatDuration = (diff: number, unit: number, singular: string, plural: string): string | null => {
      const value = Math.floor(diff / unit);
      if (value < unit) {
        if (value === 1) {
          return `${value} ${singular} ago`;
        } else {
          return `${value} ${plural} ago`;
        }
      }
      return null;
    };
  
    if (diffInSeconds < SECONDS_IN_A_MINUTE) {
      return 'just now';
    }
  
    const formattedDuration =
      formatDuration(diffInSeconds, SECONDS_IN_A_MINUTE, 'minute', 'minutes') ||
      formatDuration(diffInSeconds, SECONDS_IN_AN_HOUR, 'hour', 'hours') ||
      formatDuration(diffInSeconds, SECONDS_IN_A_DAY, 'day', 'days') ||
      formatDuration(diffInSeconds, SECONDS_IN_A_WEEK, 'week', 'weeks') ||
      formatDuration(diffInSeconds, SECONDS_IN_A_MONTH, 'month', 'months');
  
    if (formattedDuration) {
      return formattedDuration;
    }
  
    const years = Math.floor(diffInSeconds / SECONDS_IN_A_YEAR);
    return `${years} year${years === 1 ? '' : 's'} ago`;
  }
}

type RepoPage = 
  | 'security' 
  | 'dependabot' 
  | 'codeScanning' 
  | 'secretScanning' 
  | 'branches' 
  | 'lastCommit';
