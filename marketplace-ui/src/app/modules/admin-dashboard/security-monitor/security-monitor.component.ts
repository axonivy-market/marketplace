import {
  Component,
  Inject,
  inject,
  OnInit,
  PLATFORM_ID,
  ViewEncapsulation
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductSecurityInfo } from '../../../shared/models/product-security-info-model';
import { GITHUB_MARKET_ORG_URL, REPO_PAGE_PATHS, ERROR_MESSAGES, SECURITY_MONITOR_SESSION_DATA, TIME_UNITS, UNAUTHORIZED } from '../../../shared/constants/common.constant';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminDashboardService } from '../admin-dashboard.service';

@Component({
  selector: 'app-security-monitor',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent],
  templateUrl: './security-monitor.component.html',
  styleUrls: ['./security-monitor.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class SecurityMonitorComponent implements OnInit{
  themeService = inject(ThemeService);
  errorMessage = '';
  repos: ProductSecurityInfo[] = [];
  protected LoadingComponentId = LoadingComponentId;
  adminDashboardService = inject(AdminDashboardService);
  pageTitleService = inject(PageTitleService);
  isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.loadSessionData();
      this.fetchSecurityDetails();
      this.pageTitleService.setTitleOnLangChange('common.security.pageTitle');
    }
  }

  onSubmit(): void {
    this.fetchSecurityDetails();
  }

  private loadSessionData(): void {
    try {
      const sessionData = sessionStorage.getItem(SECURITY_MONITOR_SESSION_DATA);
      if (sessionData) {
        this.repos = JSON.parse(sessionData) as ProductSecurityInfo[];
      }
    } catch {
      this.clearSessionData();
    }
  }

  private clearSessionData(): void {
    sessionStorage.removeItem(SECURITY_MONITOR_SESSION_DATA);
  }

  private fetchSecurityDetails(): void {
    this.adminDashboardService
      .getSecurityDetails()
      .subscribe({
        next: data => this.handleSuccess(data),
        error: (err: HttpErrorResponse) => this.handleError(err)
      });
  }

  private handleSuccess(data: ProductSecurityInfo[]): void {
    this.repos = data;
    sessionStorage.setItem(SECURITY_MONITOR_SESSION_DATA, JSON.stringify(data));
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED) {
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
    } else {
      this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    }
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
    let additionalPath = '';
    if (page === 'lastCommit') {
      additionalPath = lastCommitSHA ?? '';
    }
    if (path) {
      this.navigateToPage(repoName, path, additionalPath);
    }
  }

  formatCommitDate(date: string): string {
    const now = Date.now();
    const targetDate = new Date(date).getTime();
    const diffInSeconds = Math.floor((now - targetDate) / 1000);

    if (diffInSeconds < 60) {
      return 'just now';
    }

    for (const [index, { SECONDS, SINGULAR, PLURAL }] of TIME_UNITS.entries()) {
      if (index < TIME_UNITS.length - 1 && diffInSeconds < TIME_UNITS[index + 1].SECONDS) {
        const value = Math.floor(diffInSeconds / SECONDS);
        if (value === 1) {
          return `${value} ${SINGULAR} ago`;
        } else {
          return `${value} ${PLURAL} ago`;
        }
      }
    }

    const years = Math.floor(diffInSeconds / TIME_UNITS.at(-1)!.SECONDS);
    if (years === 1) {
      return `${years} year ago`;
    } else {
      return `${years} years ago`;
    }
  }
}
