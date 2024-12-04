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
  styleUrl: './security-monitor.component.scss',
  encapsulation: ViewEncapsulation.Emulated
})
export class SecurityMonitorComponent {
  isAuthenticated: boolean = false;
  token: string = '';
  errorMessage: string = '';
  securityMonitorService = inject(SecurityMonitorService);
  repos: Repo[] = [];
  isLoading = false;

  onSubmit() {
    if (!this.token) {
      this.errorMessage = 'Token is required';
      return;
    }
    this.errorMessage = '';
    this.isLoading = true;
    this.securityMonitorService.getSecurityDetails(this.token).subscribe({
      next: (data) => {
        this.repos = data;
        this.isAuthenticated = true;
      },
      error: (err) => {
        this.errorMessage =
        err.status === 401
          ? 'Unauthorized access. (The token should contain the "org:read" scope for authentication)'
          : 'Failed to fetch security data. Check logs for details.';
        console.error(err);
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  formatCommitDate(date: string): string {
    const now = new Date().getTime();
    const targetDate = new Date(date).getTime();
    const diffInSeconds = Math.floor((now - targetDate) / 1000);
  
    if (diffInSeconds < 60) {
      return 'just now';
    }
  
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
      return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
    }
  
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    }
  
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) {
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    }
  
    const diffInWeeks = Math.floor(diffInDays / 7);
    if (diffInWeeks < 4) {
      return `${diffInWeeks} week${diffInWeeks > 1 ? 's' : ''} ago`;
    }
  
    const diffInMonths = Math.floor(diffInDays / 30);
    if (diffInMonths < 12) {
      return `${diffInMonths} month${diffInMonths > 1 ? 's' : ''} ago`;
    }
  
    const diffInYears = Math.floor(diffInMonths / 12);
    return `${diffInYears} year${diffInYears > 1 ? 's' : ''} ago`;
  }

  hasAlerts(alerts: Record<string, number>): boolean {
    return Object.keys(alerts).length > 0;
  }

  alertKeys(alerts: Record<string, number>): string[] {
    return Object.keys(alerts);
  }

  navigateToRepoSecurityPage(repoName: string): void {
    const repoSecurityUrl = `https://github.com/axonivy-market/${repoName}/security`;
    window.open(repoSecurityUrl, '_blank');
  }

  navigateToRepoDependabotPage(repoName: string): void {
    const repoSecurityDependabotUrl = `https://github.com/axonivy-market/${repoName}/security/dependabot`;
    window.open(repoSecurityDependabotUrl, '_blank');
  }

  navigateToRepoCodeScanningPage(repoName: string): void {
    const repoSecurityDependabotUrl = `https://github.com/axonivy-market/${repoName}/security/code-scanning`;
    window.open(repoSecurityDependabotUrl, '_blank');
  }

  navigateToRepoSecurityScanningPage(repoName: string): void {
    const repoSecurityDependabotUrl = `https://github.com/axonivy-market/${repoName}/security/security-scanning`;
    window.open(repoSecurityDependabotUrl, '_blank');
  }

  navigateToRepoLastCommitPage(repoName: string, lastCommitSHA: string): void {
    const repoSecurityDependabotUrl = `https://github.com/axonivy-market/${repoName}/commit/${lastCommitSHA}`;
    window.open(repoSecurityDependabotUrl, '_blank');
  }

  navigateToRepoBranchesSettingPage(repoName: string, lastCommitSHA: string): void {
    const repoSecurityDependabotUrl = `https://github.com/axonivy-market/${repoName}/settings/branches`;
    window.open(repoSecurityDependabotUrl, '_blank');
  }
}
