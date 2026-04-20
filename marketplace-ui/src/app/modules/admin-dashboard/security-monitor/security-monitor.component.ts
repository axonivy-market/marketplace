import { Component, inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductSecurityInfo } from '../../../shared/models/product-security-info-model';
import {
  GITHUB_MARKET_ORG_URL,
  REPO_PAGE_PATHS,
  ERROR_MESSAGES,
  TIME_UNITS,
  UNAUTHORIZED,
  ASCENDING,
  DESCENDING,
  ALL_ITEMS_PAGE_SIZE,
  DEFAULT_MONITORING_PAGEABLE
} from '../../../shared/constants/common.constant';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminDashboardService } from '../admin-dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { debounceTime, finalize, Subject, Subscription } from 'rxjs';
import { LanguageService } from '../../../core/services/language/language.service';
import { SecurityMonitorSortOption } from '../../../shared/enums/security-monitor-sort.enum';
import { SecurityMonitorCriteria } from '../../../shared/models/criteria.model';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';

const SEARCH_DEBOUNCE_TIME = 500;

@Component({
  selector: 'app-security-monitor',
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, TranslateModule, NgbPaginationModule],
  templateUrl: './security-monitor.component.html',
  styleUrls: ['./security-monitor.component.scss']
})
export class SecurityMonitorComponent implements OnInit, OnDestroy {
  protected readonly ALL_ITEMS_PAGE_SIZE = ALL_ITEMS_PAGE_SIZE;
  protected readonly LoadingComponentId = LoadingComponentId;

  // Column constants for sorting
  readonly COLUMN_REPO_NAME = SecurityMonitorSortOption.REPO_NAME;
  readonly COLUMN_DEPENDABOT = SecurityMonitorSortOption.DEPENDABOT_ALERTS;
  readonly COLUMN_CODE_SCANNING = SecurityMonitorSortOption.CODE_SCANNING_ALERTS;
  readonly COLUMN_SECRET_SCANNING = SecurityMonitorSortOption.SECRET_SCANNING_ALERTS;
  readonly COLUMN_BRANCH_PROTECTION = SecurityMonitorSortOption.BRANCH_PROTECTION;
  readonly COLUMN_COMMIT_DATE = SecurityMonitorSortOption.COMMIT_DATE;

  themeService = inject(ThemeService);
  adminDashboardService = inject(AdminDashboardService);
  pageTitleService = inject(PageTitleService);
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  platformId = inject(PLATFORM_ID);

  repos: ProductSecurityInfo[] = [];
  errorMessage = '';
  isLoading = false;

  // Pagination
  page = 1;
  pageSize = 10;
  totalElements = 0;

  // Sorting
  sortColumn: SecurityMonitorSortOption = SecurityMonitorSortOption.REPO_NAME;
  sortDirection = ASCENDING;

  // Search
  searchText = '';
  searchTextChanged = new Subject<string>();

  // Criteria
  criteria: SecurityMonitorCriteria = {
    searchText: '',
    sortOption: SecurityMonitorSortOption.REPO_NAME,
    sortDirection: ASCENDING,
    pageable: { ...DEFAULT_MONITORING_PAGEABLE }
  };

  private subscriptions: Subscription[] = [];

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const searchSubscription = this.searchTextChanged
        .pipe(debounceTime(SEARCH_DEBOUNCE_TIME))
        .subscribe(searchString => {
          this.criteria.searchText = searchString;
          this.resetToFirstPage();
          this.loadSecurityDetails();
        });
      this.subscriptions.push(searchSubscription);

      this.pageTitleService.setTitleOnLangChange('common.admin.securityMonitor.pageTitle');
      this.loadSecurityDetails();
    }
  }

  onSearchChanged(searchString: string): void {
    this.searchText = searchString;
    this.searchTextChanged.next(searchString);
  }

  onClearSearch(): void {
    this.searchText = '';
    this.searchTextChanged.next('');
  }

  onPageChange(newPage: number): void {
    this.page = newPage;
    this.criteria.pageable.page = newPage - 1;
    this.loadSecurityDetails();
  }

  onPageSizeChanged(newSize: number): void {
    this.pageSize = newSize;
    this.criteria.pageable.size = newSize;
    this.resetToFirstPage();
    this.loadSecurityDetails();
  }

  sortByColumn(column: SecurityMonitorSortOption): void {
    if (this.sortColumn === column) {
      this.toggleSortDirection();
    } else {
      this.sortColumn = column;
      this.sortDirection = ASCENDING;
    }
    this.criteria.sortOption = this.sortColumn;
    this.criteria.sortDirection = this.sortDirection.toUpperCase();
    this.resetToFirstPage();
    this.loadSecurityDetails();
  }

  getSortIcon(column: SecurityMonitorSortOption): string {
    if (this.sortColumn !== column) {
      return '';
    }
    return this.sortDirection === ASCENDING ? 'ti-arrow-up' : 'ti-arrow-down';
  }

  private toggleSortDirection(): void {
    this.sortDirection = this.sortDirection === ASCENDING ? DESCENDING : ASCENDING;
  }

  private resetToFirstPage(): void {
    this.page = 1;
    this.criteria.pageable.page = 0;
  }

  private loadSecurityDetails(): void {
    this.isLoading = true;

    const subscription = this.adminDashboardService
      .searchSecurityDetails(this.criteria)
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: response => {
          this.repos = response?._embedded?.productSecurityInfoList || [];
          this.totalElements = response?.page?.totalElements ?? 0;
        },
        error: err => this.handleError(err)
      });

    this.subscriptions.push(subscription);
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

    const lastUnit = TIME_UNITS.at(-1);
    if (!lastUnit) {
      return '';
    }
    const years = Math.floor(diffInSeconds / lastUnit.SECONDS);
    if (years === 1) {
      return `${years} year ago`;
    } else {
      return `${years} years ago`;
    }
  }

  ngOnDestroy(): void {
    for (const subscription of this.subscriptions) {
      subscription.unsubscribe();
    }
  }
}
