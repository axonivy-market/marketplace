import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { GithubService, Repository, TestResult } from '../github.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { delay, of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { FOCUSED_TAB, STANDARD_TAB } from '../../../shared/constants/common.constant';

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;
  let githubService: jasmine.SpyObj<GithubService>;
  let pageTitleService: jasmine.SpyObj<any>;
  let mockRepositories: Repository[];

  beforeEach(async () => {
    mockRepositories = [
      {
        repoName: 'repo1',
        productId: 'id1',
        htmlUrl: 'https://github.com/user/repo1',
        workflowInformation: [
          {
            workflowType: 'CI',
            lastBuilt: new Date('2025-07-20T12:00:00Z'),
            conclusion: 'success',
            lastBuiltRunUrl:
              'https://github.com/market/rtf-factory/actions/runs/11111',
            currentWorkflowState: 'active',
            disabledDate: null
            
          },
          {
            workflowType: 'DEV',
            lastBuilt: new Date('2025-07-21T12:00:00Z'),
            conclusion: 'failure',
            lastBuiltRunUrl:
              'https://github.com/market/rtf-factory/actions/runs/11111',
            currentWorkflowState: 'disabled_manually',
            disabledDate: new Date('2025-07-22T12:00:00Z')
          }
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
        repoName: 'repo2',
        productId: 'id2',
        htmlUrl: 'https://github.com/user/repo2',
        workflowInformation: [],
        focused: false,
        testResults: []
      },
      {
        repoName: 'repo3',
        productId: 'id3',
        htmlUrl: 'https://github.com/user/repo3',
        workflowInformation: [],
        focused: false,
        testResults: []
      }
    ];

    const githubServiceSpy = jasmine.createSpyObj('GithubService', [
      'getRepositories'
    ]);
    const pageTitleServiceSpy = jasmine.createSpyObj(PageTitleService, [
      'setTitleOnLangChange'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        MonitoringDashboardComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        { provide: GithubService, useValue: githubServiceSpy },
        { provide: PageTitleService, useValue: pageTitleServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {},
            params: of({}),
            queryParams: of({}),
            data: of({})
          }
        },
        LanguageService,
        TranslateService
      ]
    }).compileComponents();

    githubService = TestBed.inject(
      GithubService
    ) as jasmine.SpyObj<GithubService>;
    pageTitleService = TestBed.inject(
      PageTitleService
    ) as jasmine.SpyObj<PageTitleService>;
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
    expect(pageTitleService.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.monitor.dashboard.pageTitle'
    );
  });

  it('should set loading to false on ngOnInit if not browser', () => {
    component.platformId = 'server';
    component.isLoading = true;
    component.ngOnInit();
    expect(component.isLoading).toBeFalse();
  });

  it('should load and sort repositories correctly', () => {
    const unsortedRepos = [
      {
        repoName: 'zebra-repo',
        productId: 'zebra',
        focused: false,
        htmlUrl: '',
        workflowInformation: [],
        testResults: []
      },
      {
        repoName: 'alpha-repo',
        productId: 'alpha',
        focused: true,
        htmlUrl: '',
        workflowInformation: [],
        testResults: []
      },
      {
        repoName: 'beta-repo',
        productId: 'beta',
        focused: false,
        htmlUrl: '',
        workflowInformation: [],
        testResults: []
      }
    ];

    githubService.getRepositories.and.returnValue(of(unsortedRepos));

    component.loadRepositories();

    const repositories = component.repositories();
    expect(repositories[0].repoName).toBe('alpha-repo');
    expect(repositories[1].repoName).toBe('beta-repo');
    expect(repositories[2].repoName).toBe('zebra-repo');
  });

  it('should set isLoading to true when starting to load repositories', fakeAsync(() => {
    component.isLoading = false;
    githubService.getRepositories.and.returnValue(
      of(mockRepositories).pipe(delay(0))
    );

    component.loadRepositories();
    expect(component.isLoading).toBe(true);

    tick();
    expect(component.isLoading).toBe(false);
  }));

  it('should set isLoading to false after successfully loading repositories', () => {
    githubService.getRepositories.and.returnValue(of(mockRepositories));

    component.loadRepositories();

    expect(component.isLoading).toBe(false);
  });

  it('should clear error when successfully loading repositories', fakeAsync(() => {
    component.error = 'Previous error';
    githubService.getRepositories.and.returnValue(of(mockRepositories));

    component.loadRepositories();

    tick();

    expect(component.error).toBe('');
    expect(component.isLoading).toBe(false);
  }));

  it('should filter focused repositories correctly', () => {
    const focusedRepos = component.focusedRepo();

    expect(focusedRepos.length).toBe(1);
    expect(focusedRepos[0].repoName).toBe('repo1');
    expect(focusedRepos[0].focused).toBe(true);
  });

  it('should filter standard (non-focused) repositories correctly', () => {
    const standardRepos = component.standardRepo();

    expect(standardRepos.length).toBe(2);
    expect(standardRepos.every(repo => !repo.focused)).toBe(true);
  });

  it('should initialize with focused tab as active', () => {
    expect(component.activeTab).toBe(FOCUSED_TAB);
  });

  it('should set active tab to standard', () => {
    component.setActiveTab(STANDARD_TAB);

    expect(component.activeTab).toBe(STANDARD_TAB);
  });

  it('should set active tab to focused', () => {
    component.activeTab = STANDARD_TAB;
    component.setActiveTab(FOCUSED_TAB);

    expect(component.activeTab).toBe(FOCUSED_TAB);
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
    component.setActiveTab(STANDARD_TAB);
    fixture.detectChanges();

    const focusedTab = fixture.debugElement.query(By.css('#focused-tab'));
    const standardTab = fixture.debugElement.query(By.css('#standard-tab'));

    expect(focusedTab.nativeElement.classList).not.toContain('active');
    expect(standardTab.nativeElement.classList).toContain('active');
  });

  it('should handle error when loading repositories', fakeAsync(() => {
    const errorMessage = 'Network error';
    githubService.getRepositories.and.returnValue(
      throwError(() => new Error(errorMessage)).pipe(delay(0))
    );

    component.loadRepositories();
    tick();

    expect(component.error).toBe(errorMessage);
    expect(component.isLoading).toBe(false);
  }));

  it('should display error message when error exists', () => {
    const errorMessage = 'Test error message';
    component.error = errorMessage;
    component.isLoading = false;
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('.error'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain(errorMessage);
  });

  describe('ngOnInit query parameter handling', () => {
    let activatedRoute: ActivatedRoute;

    beforeEach(() => {
      activatedRoute = TestBed.inject(ActivatedRoute);
    });

    it('should set initialFilter when search query parameter is present', () => {
      const searchValue = 'test-repo';
      activatedRoute.queryParams = of({ search: searchValue });
      
      component.platformId = 'browser';
      component.ngOnInit();

      expect(component.initialFilter()).toBe(searchValue);
    });

    it('should set activeTab to STANDARD_TAB when search parameter is present', () => {
      const searchValue = 'test-repo';
      activatedRoute.queryParams = of({ search: searchValue });
      
      component.platformId = 'browser';
      component.activeTab = FOCUSED_TAB;
      component.ngOnInit();

      expect(component.activeTab).toBe(STANDARD_TAB);
    });

    it('should set activeTab to STANDARD_TAB even when search parameter is empty string', () => {
      activatedRoute.queryParams = of({ search: '' });
      
      component.platformId = 'browser';
      component.activeTab = FOCUSED_TAB;
      component.ngOnInit();

      expect(component.activeTab).toBe(FOCUSED_TAB);
    });

    it('should not set initialFilter when search query parameter is missing', () => {
      activatedRoute.queryParams = of({});
      
      component.platformId = 'browser';
      component.ngOnInit();

      expect(component.initialFilter()).toBe('');
    });

    it('should not set initialFilter when search query parameter is empty', () => {
      activatedRoute.queryParams = of({ search: '' });
      
      component.platformId = 'browser';
      component.ngOnInit();

      expect(component.initialFilter()).toBe('');
    });
  });
});
