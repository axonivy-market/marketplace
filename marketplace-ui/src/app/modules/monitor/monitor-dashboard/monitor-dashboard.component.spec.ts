import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { GithubService, Repository, ReposResponseModel } from '../github.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;
  let githubService: jasmine.SpyObj<GithubService>;
  let router: jasmine.SpyObj<Router>;
  let mockFocusedRepositories: Repository[];
  let mockStandardRepositories: Repository[];
  let mockReposResponse: ReposResponseModel;

  beforeEach(async () => {
    mockFocusedRepositories = [
      {
        name: 'repo1',
        htmlUrl: 'https://github.com/user/repo1',
        language: 'TypeScript',
        lastUpdated: '2025-07-20T12:00:00Z',
        ciBadgeUrl: 'https://example.com/badge/ci.svg',
        devBadgeUrl: 'https://example.com/badge/dev.svg',
        focusedRepo: true,
        testResults: [
          { environment: 'ALL', workflow: 'CI', count: 10, status: 'PASSED' },
          { environment: 'ALL', workflow: 'CI', count: 2, status: 'FAILED' },
          { environment: 'MOCK', workflow: 'CI', count: 5, status: 'PASSED' },
          { environment: 'MOCK', workflow: 'CI', count: 1, status: 'FAILED' },
          { environment: 'REAL', workflow: 'CI', count: 5, status: 'PASSED' },
          { environment: 'REAL', workflow: 'CI', count: 1, status: 'FAILED' },
          { environment: 'ALL', workflow: 'DEV', count: 8, status: 'PASSED' },
          { environment: 'ALL', workflow: 'DEV', count: 0, status: 'FAILED' },
          { environment: 'MOCK', workflow: 'DEV', count: 4, status: 'PASSED' },
          { environment: 'MOCK', workflow: 'DEV', count: 0, status: 'FAILED' },
          { environment: 'REAL', workflow: 'DEV', count: 4, status: 'PASSED' },
          { environment: 'REAL', workflow: 'DEV', count: 0, status: 'FAILED' }
        ]
      }
    ];
    mockStandardRepositories = [
      {
        name: 'repo2',
        htmlUrl: 'https://github.com/user/repo2',
        language: 'Java',
        lastUpdated: '2025-07-19T12:00:00Z',
        ciBadgeUrl: 'https://example.com/badge/ci2.svg',
        devBadgeUrl: '',
        focusedRepo: false,
        testResults: [
          { environment: 'ALL', workflow: 'CI', count: 15, status: 'PASSED' },
          { environment: 'ALL', workflow: 'CI', count: 5, status: 'FAILED' },
          { environment: 'REAL', workflow: 'CI', count: 8, status: 'PASSED' },
          { environment: 'REAL', workflow: 'CI', count: 2, status: 'FAILED' }
        ]
      },
      {
        name: 'repo3',
        htmlUrl: 'https://github.com/user/repo3',
        language: 'Python',
        lastUpdated: '2025-07-18T12:00:00Z',
        ciBadgeUrl: '',
        devBadgeUrl: '',
        focusedRepo: false,
        testResults: []
      }
    ];
    mockReposResponse = {
      focusedRepos: mockFocusedRepositories,
      standardRepos: mockStandardRepositories
    };

    const githubServiceSpy = jasmine.createSpyObj('GithubService', [
      'getRepositories'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        MonitoringDashboardComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: GithubService, useValue: githubServiceSpy },
        { provide: Router, useValue: routerSpy },
        LanguageService,
        TranslateService
      ]
    }).compileComponents();

    githubService = TestBed.inject(
      GithubService
    ) as jasmine.SpyObj<GithubService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    githubService.getRepositories.and.returnValue(of(mockReposResponse));

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load focused and standard repositories on init', () => {
    expect(githubService.getRepositories).toHaveBeenCalled();
    expect(component.focusedRepos).toEqual(mockFocusedRepositories);
    expect(component.standardRepos).toEqual(mockStandardRepositories);
    expect(component.loading).toBeFalse();
  });

  it('should handle error when loading repositories', () => {
    const errorMessage = 'Network error';
    githubService.getRepositories.and.returnValue(
      throwError(() => new Error(errorMessage))
    );
    component.ngOnInit();
    expect(component.error).toBe('Failed to load repositories');
    expect(component.loading).toBeFalse();
  });

  it('should navigate to report page on badge click', () => {
    const repoName = 'test-repo';
    const workflow = 'ci';

    component.onBadgeClick(repoName, workflow);

    expect(router.navigate).toHaveBeenCalledWith(['/report', repoName, 'CI']);
  });

  it('should get correct test count for specific criteria', () => {
    const repo = mockFocusedRepositories[0];
    expect(component.getTestCount(repo, 'CI', 'ALL', 'PASSED')).toBe(10);
    expect(component.getTestCount(repo, 'CI', 'ALL', 'FAILED')).toBe(2);
    expect(component.getTestCount(repo, 'CI', 'MOCK', 'PASSED')).toBe(5);
    expect(component.getTestCount(repo, 'CI', 'MOCK', 'FAILED')).toBe(1);
    expect(component.getTestCount(repo, 'CI', 'REAL', 'PASSED')).toBe(5);
    expect(component.getTestCount(repo, 'CI', 'REAL', 'FAILED')).toBe(1);
    expect(component.getTestCount(repo, 'DEV', 'ALL', 'PASSED')).toBe(8);
    expect(component.getTestCount(repo, 'DEV', 'ALL', 'FAILED')).toBe(0);
  });

  it('should return 0 for missing test results', () => {
    const repo = mockStandardRepositories[1];
    expect(component.getTestCount(repo, 'CI', 'ALL', 'PASSED')).toBe(0);
    expect(component.getTestCount(repo, 'DEV', 'MOCK', 'FAILED')).toBe(0);
  });

  it('should return 0 for non-matching test criteria', () => {
    const repo = mockFocusedRepositories[0];
    expect(component.getTestCount(repo, 'NONEXISTENT', 'ALL', 'PASSED')).toBe(
      0
    );
    expect(component.getTestCount(repo, 'CI', 'NONEXISTENT', 'PASSED')).toBe(0);
    expect(component.getTestCount(repo, 'CI', 'ALL', 'NONEXISTENT')).toBe(0);
  });

  it('should handle repositories with no testResults property', () => {
    const repo: Repository = {
      name: 'no-tests-repo',
      htmlUrl: 'https://github.com/user/no-tests-repo',
      language: 'JavaScript',
      lastUpdated: '2025-07-17T12:00:00Z',
      ciBadgeUrl: 'https://example.com/badge/ci3.svg',
      devBadgeUrl: 'https://example.com/badge/dev3.svg',
      focusedRepo: false
    };
    expect(component.getTestCount(repo, 'CI', 'ALL', 'PASSED')).toBe(0);
    expect(component.getTestCount(repo, 'DEV', 'MOCK', 'FAILED')).toBe(0);
  });

  it('should display repository cards when data is loaded', () => {
    fixture.detectChanges();
    const repoCards = fixture.debugElement.queryAll(By.css('.repo-card'));
    expect(repoCards.length).toBe(
      mockFocusedRepositories.length + mockStandardRepositories.length
    );
  });

  it('should display loading message when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const loadingElement = fixture.debugElement.query(By.css('.loading'));
    expect(loadingElement).toBeTruthy();
  });

  it('should display error message when error exists', () => {
    const errorMessage = 'Test error message';
    component.error = errorMessage;
    component.loading = false;
    fixture.detectChanges();
    const errorElement = fixture.debugElement.query(By.css('.error'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain(errorMessage);
  });

  it('should show CI badge section only when ciBadgeUrl exists', () => {
    fixture.detectChanges();
    const firstRepoCard = fixture.debugElement.queryAll(
      By.css('.repo-card')
    )[0];
    const ciBadgeRow = firstRepoCard.query(By.css('.badge-row'));
    expect(ciBadgeRow).toBeTruthy();
    const lastRepoCard = fixture.debugElement.queryAll(By.css('.repo-card'))[2];
    const noBadgeRow = lastRepoCard.query(By.css('.badge-row'));
    expect(noBadgeRow).toBeFalsy();
  });

  it('should show DEV badge section only when devBadgeUrl exists', () => {
    fixture.detectChanges();
    const firstRepoCard = fixture.debugElement.queryAll(
      By.css('.repo-card')
    )[0];
    const devBadgeRow = firstRepoCard.queryAll(By.css('.badge-row'))[1];
    expect(devBadgeRow).toBeTruthy();
    const lastRepoCard = fixture.debugElement.queryAll(By.css('.repo-card'))[2];
    const noDevBadgeRow = lastRepoCard.query(By.css('.badge-row'));
    expect(noDevBadgeRow).toBeFalsy();
  });

  it('should return repo.name in trackByName', () => {
    const repo = mockFocusedRepositories[0];
    expect(component.trackByName(0, repo)).toBe(repo.name);
  });

  it('should handle empty focusedRepos and standardRepos', () => {
    component.focusedRepos = [];
    component.standardRepos = [];
    fixture.detectChanges();
    const repoCards = fixture.debugElement.queryAll(By.css('.repo-card'));
    expect(repoCards.length).toBe(0);
  });

  it('should display loading message when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();

    const loadingElement = fixture.debugElement.query(By.css('.loading'));
    expect(loadingElement).toBeTruthy();
  });

  it('should display error message when error exists', () => {
    const errorMessage = 'Test error message';
    component.error = errorMessage;
    component.loading = false;
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('.error'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain(errorMessage);
  });

  it('should show CI badge section only when ciBadgeUrl exists', () => {
    fixture.detectChanges();

    const firstRepoCard = fixture.debugElement.queryAll(
      By.css('.repo-card')
    )[0];
    const ciBadgeRow = firstRepoCard.query(By.css('.badge-row:first-of-type'));
    expect(ciBadgeRow).toBeTruthy();

    const thirdRepoCard = fixture.debugElement.queryAll(
      By.css('.repo-card')
    )[2];
    const noBadgeRow = thirdRepoCard.query(By.css('.badge-row'));
    expect(noBadgeRow).toBeFalsy();
  });
});
