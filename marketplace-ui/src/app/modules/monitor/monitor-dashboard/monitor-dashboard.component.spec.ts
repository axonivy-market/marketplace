import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { GithubService, Repository, TestResult } from '../github.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;
  let githubService: jasmine.SpyObj<GithubService>;
  let router: jasmine.SpyObj<Router>;
  let pageTitleService: jasmine.SpyObj<any>;
  let mockRepositories: Repository[];

  beforeEach(async () => {
    mockRepositories = [
      {
        name: 'repo1',
        htmlUrl: 'https://github.com/user/repo1',
        workflowInformation: [
          {
            workflowType: 'CI',
            lastBuilt: new Date('2025-07-20T12:00:00Z'),
            conclusion: 'success',
            lastBuiltRun: 'https://github.com/market/rtf-factory/actions/runs/11111'
          },
          {
            workflowType: 'DEV',
            lastBuilt: new Date('2025-07-21T12:00:00Z'),
            conclusion: 'failure',
            lastBuiltRun: 'https://github.com/market/rtf-factory/actions/runs/11111'
          },
        ],
        focused: true,
        testResults: [
          {
            workflow: 'CI',
            results: { PASSED: 20 }
          } as TestResult
        ]
      },
      {
        name: 'repo2',
        htmlUrl: 'https://github.com/user/repo2',
        workflowInformation: [],
        focused: false,
        testResults: []
      },
      {
        name: 'repo3',
        htmlUrl: 'https://github.com/user/repo3',
        workflowInformation: [],
        focused: false,
        testResults: []
      }
    ];

    const githubServiceSpy = jasmine.createSpyObj('GithubService', ['getRepositories']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const pageTitleServiceSpy = jasmine.createSpyObj(PageTitleService, ['setTitleOnLangChange']);

    await TestBed.configureTestingModule({
      imports: [
        MonitoringDashboardComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        { provide: GithubService, useValue: githubServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: PageTitleService, useValue: pageTitleServiceSpy },
        LanguageService,
        TranslateService
      ]
    }).compileComponents();

    githubService = TestBed.inject(GithubService) as jasmine.SpyObj<GithubService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    pageTitleService = TestBed.inject(PageTitleService) as jasmine.SpyObj<PageTitleService>;
    githubService.getRepositories.and.returnValue(of(mockRepositories));

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load repositories on init', () => {
    component.platformId = 'browser';
    component.pageTitleService = pageTitleService;
    const loadRepositoriesSpy = spyOn(component, 'loadRepositories');
    component.ngOnInit();
    expect(loadRepositoriesSpy).toHaveBeenCalled();
    expect(pageTitleService.setTitleOnLangChange).toHaveBeenCalledWith('common.monitor.dashboard.pageTitle');
  });

  it('should set loading to false on ngOnInit if not browser', () => {
    component.platformId = 'server';
    component.loading = true;
    component.ngOnInit();
    expect(component.loading).toBeFalse();
  });

  it('should load and sort repositories correctly', () => {
    const unsortedRepos = [
      { name: 'zebra-repo', focused: false, htmlUrl: '', workflowInformation: [], testResults: [] },
      { name: 'alpha-repo', focused: true, htmlUrl: '', workflowInformation: [], testResults: [] },
      { name: 'beta-repo', focused: false, htmlUrl: '', workflowInformation: [], testResults: [] }
    ];

    githubService.getRepositories.and.returnValue(of(unsortedRepos));

    component.loadRepositories();

    const repositories = component.repositories();
    expect(repositories[0].name).toBe('alpha-repo');
    expect(repositories[1].name).toBe('beta-repo');
    expect(repositories[2].name).toBe('zebra-repo');
  });

  it('should set isLoading to true when starting to load repositories', () => {
    component.isLoading = false;
    githubService.getRepositories.and.returnValue(of(mockRepositories));

    component.loadRepositories();

    expect(component.isLoading).toBe(true);
  });

  it('should set isLoading to false after successfully loading repositories', () => {
    githubService.getRepositories.and.returnValue(of(mockRepositories));

    component.loadRepositories();

    expect(component.isLoading).toBe(false);
  });

  it('should clear error when successfully loading repositories', () => {
    component.error = 'Previous error';
    githubService.getRepositories.and.returnValue(of(mockRepositories));

    component.loadRepositories();

    expect(component.error).toBe('');
  });

  it('should filter focused repositories correctly', () => {
    const focusedRepos = component.focusedRepo();

    expect(focusedRepos.length).toBe(1);
    expect(focusedRepos[0].name).toBe('repo1');
    expect(focusedRepos[0].focused).toBe(true);
  });

  it('should filter standard (non-focused) repositories correctly', () => {
    const standardRepos = component.standardRepo();

    expect(standardRepos.length).toBe(2);
    expect(standardRepos.every(repo => !repo.focused)).toBe(true);
  });

  it('should initialize with focused tab as active', () => {
    expect(component.activeTab).toBe('focused');
  });

  it('should set active tab to standard', () => {
    component.setActiveTab('standard');

    expect(component.activeTab).toBe('standard');
  });

  it('should set active tab to focused', () => {
    component.activeTab = 'standard';
    component.setActiveTab('focused');

    expect(component.activeTab).toBe('focused');
  });

  it('should handle invalid tab name', () => {
    component.setActiveTab('invalid-tab');

    expect(component.activeTab).toBe('invalid-tab');
  });

  it('should render title with correct translation key', () => {
    const titleElement = fixture.debugElement.query(By.css('h1.title'));
    expect(titleElement).toBeTruthy();
  });

  it('should render monitoring wiki link with correct href', () => {
    const wikiLink = fixture.debugElement.query(By.css('a[target="_blank"]'));
    expect(wikiLink).toBeTruthy();
    expect(wikiLink.nativeElement.href).toBe(component.monitoringWikiLink);
  });

  it('should render both tab navigation items', () => {
    const tabItems = fixture.debugElement.queryAll(By.css('.nav-item'));
    expect(tabItems.length).toBe(2);
  });

  it('should have focused tab active by default', () => {
    const focusedTab = fixture.debugElement.query(By.css('#focused-tab'));
    expect(focusedTab.nativeElement.classList).toContain('active');
  });

  it('should switch tab classes when activeTab changes', () => {
    component.setActiveTab('standard');
    fixture.detectChanges();

    const focusedTab = fixture.debugElement.query(By.css('#focused-tab'));
    const standardTab = fixture.debugElement.query(By.css('#standard-tab'));

    expect(focusedTab.nativeElement.classList).not.toContain('active');
    expect(standardTab.nativeElement.classList).toContain('active');
  });

  it('should handle error when loading repositories', () => {
    const errorMessage = 'Network error';
    githubService.getRepositories.and.returnValue(throwError(() => new Error(errorMessage)));

    component.loadRepositories();

    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBeFalse();
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
});
