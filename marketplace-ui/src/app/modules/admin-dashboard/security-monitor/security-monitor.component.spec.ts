import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SecurityMonitorComponent } from './security-monitor.component';
import { TranslateModule } from '@ngx-translate/core';
import {
  ASCENDING,
  DESCENDING,
  ERROR_MESSAGES,
  TIME_UNITS,
  UNAUTHORIZED
} from '../../../shared/constants/common.constant';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { AdminDashboardService } from '../admin-dashboard.service';
import { of, Subscription, throwError } from 'rxjs';
import { SecurityMonitorSortOption } from '../../../shared/enums/security-monitor-sort.enum';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';

describe('SecurityMonitorComponent', () => {
  let component: SecurityMonitorComponent;
  let fixture: ComponentFixture<SecurityMonitorComponent>;
  let adminDashboardServiceSpy: { searchSecurityDetails: ReturnType<typeof vi.fn> };
  let pageTitleSpy: { setTitleOnLangChange: ReturnType<typeof vi.fn> };

  const apiResponse = {
    _embedded: {
      productSecurityInfoList: [
        {
          repoName: 'repo-a',
          visibility: 'public',
          archived: false,
          branchProtectionEnabled: true,
          lastCommitDate: new Date().toISOString(),
          latestCommitSHA: 'abc123',
          dependabot: { status: 'ACTIVE', alerts: {} },
          codeScanning: { status: 'ACTIVE', alerts: {} },
          secretScanning: { status: 'ACTIVE', numberOfAlerts: 0 }
        }
      ]
    },
    page: {
      totalElements: 1
    }
  };

  beforeEach(async () => {
    adminDashboardServiceSpy = {
      searchSecurityDetails: vi
        .fn()
        .mockName('AdminDashboardService.searchSecurityDetails')
    };
    pageTitleSpy = {
      setTitleOnLangChange: vi
        .fn()
        .mockName('PageTitleService.setTitleOnLangChange')
    };
    const languageServiceSpy = {
      selectedLanguage: vi.fn().mockReturnValue('en')
    };
    const themeServiceSpy = {
      isDarkMode: vi.fn().mockReturnValue(false)
    };

    adminDashboardServiceSpy.searchSecurityDetails.mockReturnValue(of(apiResponse));

    await TestBed.configureTestingModule({
      imports: [SecurityMonitorComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AdminDashboardService, useValue: adminDashboardServiceSpy },
        { provide: PageTitleService, useValue: pageTitleSpy },
        { provide: LanguageService, useValue: languageServiceSpy },
        { provide: ThemeService, useValue: themeServiceSpy }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(SecurityMonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create the component', () => {
    createComponent();
    expect(component).toBeTruthy();
    expect(pageTitleSpy.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.admin.securityMonitor.pageTitle'
    );
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(1);
    expect(component.repos.length).toBe(1);
    expect(component.totalElements).toBe(1);
  });

  it('should navigate to the correct URL for a repo page', () => {
    createComponent();
    vi.spyOn(window, 'open');
    component.navigateToRepoPage('example-repo', 'secretScanning');
    expect(window.open).toHaveBeenCalledWith(
      'https://github.com/axonivy-market/example-repo/security/secret-scanning',
      '_blank'
    );

    component.navigateToRepoPage('example-repo', 'lastCommit', 'abc123');
    expect(window.open).toHaveBeenCalledWith(
      'https://github.com/axonivy-market/example-repo/commit/abc123',
      '_blank'
    );
  });

  it('should handle empty alerts correctly in hasAlerts', () => {
    createComponent();
    expect(component.hasAlerts({})).toBe(false);
    expect(component.hasAlerts({ alert1: 1 })).toBe(true);
  });

  it('should return correct alert keys from alertKeys', () => {
    createComponent();
    const alerts = { alert1: 1, alert2: 2 };
    expect(component.alertKeys(alerts)).toEqual(['alert1', 'alert2']);
  });

  it('should return "just now" for dates less than 60 seconds ago', () => {
    createComponent();
    const recentDate = new Date(new Date().getTime() - 30 * 1000).toISOString();
    const result = component.formatCommitDate(recentDate);
    expect(result).toBe('just now');
  });

  it('should return "1 minute ago" for dates 1 minute ago', () => {
    createComponent();
    const oneMinuteAgo = new Date(
      new Date().getTime() - 60 * 1000
    ).toISOString();
    TIME_UNITS[0] = { SECONDS: 60, SINGULAR: 'minute', PLURAL: 'minutes' };
    const result = component.formatCommitDate(oneMinuteAgo);
    expect(result).toBe('1 minute ago');
  });

  it('should return "1 year ago" for dates exactly 1 year ago', () => {
    createComponent();
    const oneYearAgo = new Date(
      new Date().getTime() - 365 * 24 * 60 * 60 * 1000
    ).toISOString();
    const result = component.formatCommitDate(oneYearAgo);
    expect(result).toBe('1 year ago');
  });

  it('should return "2 years ago" for dates 2 years ago', () => {
    createComponent();
    const twoYearsAgo = new Date(
      new Date().getTime() - 2 * 365 * 24 * 60 * 60 * 1000
    ).toISOString();
    const result = component.formatCommitDate(twoYearsAgo);
    expect(result).toBe('2 years ago');
  });

  it('should return empty string when TIME_UNITS is empty', () => {
    createComponent();
    const originalUnits = [...TIME_UNITS];
    TIME_UNITS.length = 0;

    const oneYearAgo = new Date(
      new Date().getTime() - 365 * 24 * 60 * 60 * 1000
    ).toISOString();
    const result = component.formatCommitDate(oneYearAgo);

    expect(result).toBe('');

    TIME_UNITS.push(...originalUnits);
  });

  it('should debounce search changes before loading data', () => {
    vi.useFakeTimers();
    createComponent();
    adminDashboardServiceSpy.searchSecurityDetails.mockClear();

    component.onSearchChanged('repo');

    expect(component.searchText).toBe('repo');
    expect(adminDashboardServiceSpy.searchSecurityDetails).not.toHaveBeenCalled();

    vi.advanceTimersByTime(499);
    expect(adminDashboardServiceSpy.searchSecurityDetails).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(1);
    expect(component.criteria.searchText).toBe('repo');
    expect(component.page).toBe(1);
    expect(component.criteria.pageable.page).toBe(0);
  });

  it('should clear search text with debounce pipeline', () => {
    vi.useFakeTimers();
    createComponent();
    adminDashboardServiceSpy.searchSecurityDetails.mockClear();

    component.searchText = 'something';
    component.onClearSearch();

    expect(component.searchText).toBe('');
    vi.advanceTimersByTime(500);
    expect(component.criteria.searchText).toBe('');
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(1);
  });

  it('should update pagination criteria on page change', () => {
    createComponent();
    adminDashboardServiceSpy.searchSecurityDetails.mockClear();

    component.onPageChange(3);

    expect(component.page).toBe(3);
    expect(component.criteria.pageable.page).toBe(2);
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(1);
  });

  it('should update page size and reset to first page', () => {
    createComponent();
    adminDashboardServiceSpy.searchSecurityDetails.mockClear();
    component.page = 4;
    component.criteria.pageable.page = 3;

    component.onPageSizeChanged(20);

    expect(component.pageSize).toBe(20);
    expect(component.criteria.pageable.size).toBe(20);
    expect(component.page).toBe(1);
    expect(component.criteria.pageable.page).toBe(0);
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(1);
  });

  it('should sort by selected column and toggle direction on repeated selection', () => {
    createComponent();
    adminDashboardServiceSpy.searchSecurityDetails.mockClear();

    component.sortByColumn(SecurityMonitorSortOption.CODE_SCANNING_ALERTS);

    expect(component.sortColumn).toBe(SecurityMonitorSortOption.CODE_SCANNING_ALERTS);
    expect(component.sortDirection).toBe(ASCENDING);
    expect(component.criteria.sortDirection).toBe(ASCENDING.toUpperCase());
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(1);

    component.sortByColumn(SecurityMonitorSortOption.CODE_SCANNING_ALERTS);

    expect(component.sortDirection).toBe(DESCENDING);
    expect(component.criteria.sortDirection).toBe(DESCENDING.toUpperCase());
    expect(adminDashboardServiceSpy.searchSecurityDetails).toHaveBeenCalledTimes(2);
  });

  it('should return the correct sort icon', () => {
    createComponent();
    component.sortColumn = SecurityMonitorSortOption.REPO_NAME;
    component.sortDirection = ASCENDING;

    expect(component.getSortIcon(SecurityMonitorSortOption.REPO_NAME)).toBe('ti-arrow-up');

    component.sortDirection = DESCENDING;
    expect(component.getSortIcon(SecurityMonitorSortOption.REPO_NAME)).toBe('ti-arrow-down');

    expect(component.getSortIcon(SecurityMonitorSortOption.SECRET_SCANNING_ALERTS)).toBe('');
  });

  it('should set unauthorized error message when API returns 401', () => {
    adminDashboardServiceSpy.searchSecurityDetails.mockReturnValueOnce(
      throwError(() => ({ status: UNAUTHORIZED }))
    );

    createComponent();

    expect(component.errorMessage).toBe(ERROR_MESSAGES.INVALID_TOKEN);
  });

  it('should set fetch error message on non-401 API error', () => {
    adminDashboardServiceSpy.searchSecurityDetails.mockReturnValueOnce(
      throwError(() => ({ status: 500 }))
    );

    createComponent();

    expect(component.errorMessage).toBe(ERROR_MESSAGES.FETCH_FAILURE);
  });

  it('should unsubscribe all active subscriptions on destroy', () => {
    createComponent();
    const unsubscribeSpy = vi.spyOn(Subscription.prototype, 'unsubscribe');

    component.ngOnDestroy();

    expect(unsubscribeSpy).toHaveBeenCalled();
    unsubscribeSpy.mockRestore();
  });

});
