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
          { workflowType: 'CI',
            lastBuilt: new Date('2025-07-20T12:00:00Z'),
            conclusion: 'success',
            lastBuiltRun: 'https://github.com/market/rtf-factory/actions/runs/11111' },
          { workflowType: 'DEV',
            lastBuilt: new Date('2025-07-21T12:00:00Z'),
            conclusion: 'failure',
            lastBuiltRun: 'https://github.com/market/rtf-factory/actions/runs/11111' },
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
        TranslateModule.forRoot()
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
