import { Component, inject, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { SecurityMonitorService } from './security-monitor.service';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { GITHUB_MARKET_ORG_URL, REPO_PAGE_PATHS, SECURITY_MONITOR_MESSAGES, SECURITY_MONITOR_SESSION_KEYS, TIME_UNITS, UNAUTHORIZED } from '../../shared/constants/common.constant';

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

  ngOnInit(): void {
    this.loadSessionData();
  }

  onSubmit(): void {
    this.token = this.token ?? sessionStorage.getItem(SECURITY_MONITOR_SESSION_KEYS.TOKEN) ?? '';
    if (!this.token) {
      this.handleMissingToken();
      return;
    }

    this.errorMessage = '';
    this.fetchSecurityDetails();
  }

  private loadSessionData(): void {
    const sessionData = sessionStorage.getItem(SECURITY_MONITOR_SESSION_KEYS.DATA);
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
    this.errorMessage = SECURITY_MONITOR_MESSAGES.TOKEN_REQUIRED;
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
    sessionStorage.setItem(SECURITY_MONITOR_SESSION_KEYS.TOKEN, this.token);
    sessionStorage.setItem(SECURITY_MONITOR_SESSION_KEYS.DATA, JSON.stringify(data));
  }

  private handleError(err: HttpErrorResponse): void {
    this.errorMessage = err.status === UNAUTHORIZED
      ? SECURITY_MONITOR_MESSAGES.UNAUTHORIZED_ACCESS
      : SECURITY_MONITOR_MESSAGES.FETCH_FAILURE;

    this.isAuthenticated = false;
    this.clearSessionData();
  }

  private clearSessionData(): void {
    sessionStorage.removeItem(SECURITY_MONITOR_SESSION_KEYS.TOKEN);
    sessionStorage.removeItem(SECURITY_MONITOR_SESSION_KEYS.DATA);
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

  navigateToRepoPage(repoName: string, page: keyof typeof REPO_PAGE_PATHS, lastCommitSHA?: string): void {
    const path = REPO_PAGE_PATHS[page];
    if (path) {
      this.navigateToPage(repoName, path, page === 'lastCommit' ? lastCommitSHA ?? '' : '');
    }
  }

  formatCommitDate(date: string): string {
    const now = new Date().getTime();
    const targetDate = new Date(date).getTime();
    const diffInSeconds = Math.floor((now - targetDate) / 1000);

    if (diffInSeconds < 60) {
      return 'just now';
    }

    for (const { SECONDS, SINGULAR, PLURAL } of TIME_UNITS) {
      if (diffInSeconds < SECONDS) {
        const value = Math.floor(diffInSeconds / (SECONDS / 60));
        return value === 1 ? `${value} ${SINGULAR} ago` : `${value} ${PLURAL} ago`;
      }
    }

    const years = Math.floor(diffInSeconds / TIME_UNITS[TIME_UNITS.length - 1].SECONDS);
    return years === 1 ? `${years} year ago` : `${years} years ago`;
  }
}
