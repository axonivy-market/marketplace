import { Component, inject, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SecurityMonitorService } from './security-monitor.service';
import { Repo } from '../../shared/models/security-model';
import { CommonModule } from '@angular/common';

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
  repos: Repo[] = [];
  isLoading = false;

  private securityMonitorService = inject(SecurityMonitorService);
  private readonly githubBaseUrl = 'https://github.com/axonivy-market';

  ngOnInit(): void {
    try {
      const sessionData = sessionStorage.getItem('security-monitor-data');
      if (sessionData) {
        this.repos = JSON.parse(sessionData) as Repo[];
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
    if (!this.token) {
      this.errorMessage = 'Token is required';
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.securityMonitorService.getSecurityDetails(this.token).subscribe({
      next: (data) => this.handleSuccess(data),
      error: (err) => this.handleError(err),
      complete: () => (this.isLoading = false),
    });
  }

  private handleSuccess(data: Repo[]): void {
    this.repos = data;
    this.isAuthenticated = true;
    this.isLoading = false;
    sessionStorage.setItem('security-monitor-token', this.token);
    sessionStorage.setItem('security-monitor-data', JSON.stringify(data));
  }

  private handleError(err: any): void {
    this.errorMessage =
      err.status === 401
        ? 'Unauthorized access.'
        : 'Failed to fetch security data. Check logs for details.';
    console.error(err);
    this.isLoading = false;
  }

  formatCommitDate(date: string): string {
    const now = Date.now();
    const targetDate = new Date(date).getTime();
    const diffInSeconds = Math.floor((now - targetDate) / 1000);

    if (diffInSeconds < 60) return 'just now';
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) return this.formatTime(diffInMinutes, 'minute');
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return this.formatTime(diffInHours, 'hour');
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) return this.formatTime(diffInDays, 'day');
    const diffInWeeks = Math.floor(diffInDays / 7);
    if (diffInWeeks < 4) return this.formatTime(diffInWeeks, 'week');
    const diffInMonths = Math.floor(diffInDays / 30);
    if (diffInMonths < 12) return this.formatTime(diffInMonths, 'month');
    const diffInYears = Math.floor(diffInMonths / 12);
    return this.formatTime(diffInYears, 'year');
  }

  private formatTime(value: number, unit: string): string {
    return `${value} ${unit}${value > 1 ? 's' : ''} ago`;
  }

  hasAlerts(alerts: Record<string, number>): boolean {
    return Object.keys(alerts).length > 0;
  }

  alertKeys(alerts: Record<string, number>): string[] {
    return Object.keys(alerts);
  }

  navigateToPage(repoName: string, path: string, additionalPath: string = ''): void {
    const url = `${this.githubBaseUrl}/${repoName}${path}${additionalPath}`;
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
