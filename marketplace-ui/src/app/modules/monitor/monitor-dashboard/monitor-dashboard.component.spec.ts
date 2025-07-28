import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { GithubService, Repository } from '../github.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { of,  } from 'rxjs';
import { PLATFORM_ID } from '@angular/core';
import { SortOptionLabel } from '../../../shared/enums/sort-option.enum';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;
  let githubService: jasmine.SpyObj<GithubService>;
  let router: jasmine.SpyObj<Router>;
  let mockRepositories: Repository[];

  beforeEach(async () => {
    mockRepositories = [
      {
        name: 'repo1',
        htmlUrl: 'https://github.com/user/repo1',
        language: 'TypeScript',
        lastUpdated: '2025-07-20T12:00:00Z',
        ciBadgeUrl: 'https://example.com/badge/ci.svg',
        devBadgeUrl: 'https://example.com/badge/dev.svg',
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
      },
      {
        name: 'repo2',
        htmlUrl: 'https://github.com/user/repo2',
        language: 'Java',
        lastUpdated: '2025-07-19T12:00:00Z',
        ciBadgeUrl: 'https://example.com/badge/ci2.svg',
        devBadgeUrl: '',
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
        testResults: []
      }
    ];

    const githubServiceSpy = jasmine.createSpyObj('GithubService', ['getRepositories', 'getFocusedRepositories', 'getStandardRepositories']);
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
        { provide: PLATFORM_ID, useValue: 'browser' },
        LanguageService,
        TranslateService
      ]
    }).compileComponents();

    githubService = TestBed.inject(GithubService) as jasmine.SpyObj<GithubService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    githubService.getFocusedRepositories.and.returnValue(of(mockRepositories));
    githubService.getStandardRepositories.and.returnValue(of(mockRepositories));

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch focused repositories', () => {
    githubService.getFocusedRepositories.and.returnValue(of(mockRepositories));
    component.fetchRepositoriesBySort('FOCUSED' as SortOptionLabel);
    expect(component.repositories).toEqual(mockRepositories);
    expect(component.loading).toBeFalse();
  });

  it('should fetch standard repositories', () => {
    const standardRepos = [mockRepositories[1]];
    githubService.getStandardRepositories.and.returnValue(of(standardRepos));
    component.fetchRepositoriesBySort('STANDARD' as SortOptionLabel);
    expect(component.repositories).toEqual(standardRepos);
    expect(component.loading).toBeFalse();
  });

  it('should get correct test count for specific criteria', () => {
    const repo = mockRepositories[0];
    expect(component.getTestCount(repo, 'CI', 'ALL', 'PASSED')).toBe(10);
    expect(component.getTestCount(repo, 'CI', 'ALL', 'FAILED')).toBe(2);
    expect(component.getTestCount(repo, 'DEV', 'MOCK', 'PASSED')).toBe(4);
    expect(component.getTestCount(repo, 'DEV', 'MOCK', 'FAILED')).toBe(0);
  });

  it('should return 0 for missing testResults', () => {
    const repo = mockRepositories[2];
    expect(component.getTestCount(repo, 'CI', 'ALL', 'PASSED')).toBe(0);
    expect(component.getTestCount(repo, 'DEV', 'MOCK', 'FAILED')).toBe(0);
  });

  it('should return 0 for non-matching test criteria', () => {
    const repo = mockRepositories[0];
    expect(component.getTestCount(repo, 'NONEXISTENT', 'ALL', 'PASSED')).toBe(0);
    expect(component.getTestCount(repo, 'CI', 'NONEXISTENT', 'PASSED')).toBe(0);
    expect(component.getTestCount(repo, 'CI', 'ALL', 'NONEXISTENT')).toBe(0);
  });

  it('should return 0 for repo with no testResults property', () => {
    const repo: Repository = {
      name: 'no-tests-repo',
      htmlUrl: '',
      language: '',
      lastUpdated: '',
      ciBadgeUrl: '',
      devBadgeUrl: ''
    };
    expect(component.getTestCount(repo, 'CI', 'ALL', 'PASSED')).toBe(0);
  });

  it('should navigate to report page on badge click', () => {
    const repoName = 'test-repo';
    const workflow = 'ci';
    component.onBadgeClick(repoName, workflow);
    expect(router.navigate).toHaveBeenCalledWith(['/report', repoName, 'CI']);
  });

  it('should emit sortChange and update selectedSort on onSortChange', () => {
    spyOn(component.sortChange, 'emit');
    const sortValue = 'STANDARD' as SortOptionLabel;
    component.sorts = [
      { label: 'Focused', value: 'FOCUSED' as SortOptionLabel },
      { label: 'Standard', value: 'STANDARD' as SortOptionLabel }
    ];
    githubService.getStandardRepositories.and.returnValue(of([]));
    component.onSortChange(sortValue);
    expect(component.selectedSort.value).toBe(sortValue);
    expect(component.selectedSortLabel).toBe('Standard');
    expect(component.sortChange.emit).toHaveBeenCalledWith(sortValue);
  });

  it('should not emit sortChange if sort not found in onSortChange', () => {
    spyOn(component.sortChange, 'emit');
    component.sorts = [
      { label: 'Focused', value: 'FOCUSED' as SortOptionLabel }
    ];
    component.onSortChange('STANDARD' as SortOptionLabel);
    expect(component.sortChange.emit).not.toHaveBeenCalled();
  });

});
