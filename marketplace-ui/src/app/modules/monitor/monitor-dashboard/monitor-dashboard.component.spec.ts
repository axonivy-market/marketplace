import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { GithubService, Repository, TestResult } from '../github.service';
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
  let mockRepositories: Repository[];

  beforeEach(async () => {
    mockRepositories = [
      {
        name: 'repo1',
        // htmlUrl: 'https://github.com/user/repo1',
        language: 'TypeScript',
        lastUpdated: '2025-07-20T12:00:00Z',
        focused: true,
        testResults: [
          // { environment: 'ALL', workflow: 'CI', count: 10, status: 'PASSED' },
          // { environment: 'ALL', workflow: 'CI', count: 2, status: 'FAILED' },
          // { environment: 'MOCK', workflow: 'CI', count: 5, status: 'PASSED' },
          // { environment: 'MOCK', workflow: 'CI', count: 1, status: 'FAILED' },
          // { environment: 'REAL', workflow: 'CI', count: 5, status: 'PASSED' },
          // { environment: 'REAL', workflow: 'CI', count: 1, status: 'FAILED' },
          // { environment: 'ALL', workflow: 'DEV', count: 8, status: 'PASSED' },
          // { environment: 'ALL', workflow: 'DEV', count: 0, status: 'FAILED' },
          // { environment: 'MOCK', workflow: 'DEV', count: 4, status: 'PASSED' },
          // { environment: 'MOCK', workflow: 'DEV', count: 0, status: 'FAILED' },
          // { environment: 'REAL', workflow: 'DEV', count: 4, status: 'PASSED' },
          // { environment: 'REAL', workflow: 'DEV', count: 0, status: 'FAILED' }
        ]
      },
      {
        name: 'repo2',
        // htmlUrl: 'https://github.com/user/repo2',
        language: 'Java',
        lastUpdated: '2025-07-19T12:00:00Z',
        focused: false,
        testResults: [
          // { environment: 'ALL', workflow: 'CI', count: 15, status: 'PASSED' },
          // { environment: 'ALL', workflow: 'CI', count: 5, status: 'FAILED' },
          // { environment: 'REAL', workflow: 'CI', count: 8, status: 'PASSED' },
          // { environment: 'REAL', workflow: 'CI', count: 2, status: 'FAILED' }
        ]
      },
      {
        name: 'repo3',
        language: 'Python',
        lastUpdated: '2025-07-18T12:00:00Z',
        focused: false,
        testResults: [] 
      }
    ];

    const githubServiceSpy = jasmine.createSpyObj('GithubService', ['getRepositories']);
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

    githubService = TestBed.inject(GithubService) as jasmine.SpyObj<GithubService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    githubService.getRepositories.and.returnValue(of(mockRepositories));

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load repositories on init', () => {
    expect(githubService.getRepositories).toHaveBeenCalled();
    expect(component.repositories).toEqual(mockRepositories);
    expect(component.loading).toBeFalse();
  });

  it('should handle error when loading repositories', () => {
    const errorMessage = 'Network error';
    githubService.getRepositories.and.returnValue(throwError(() => new Error(errorMessage)));
    
    component.loadRepositories();
    
    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBeFalse();
  });

  it('should navigate to report page on badge click', () => {
    const repoName = 'test-repo';
    const workflow = 'ci';
    
    component.onBadgeClick(repoName, workflow);
    
    expect(router.navigate).toHaveBeenCalledWith(['/report', repoName, 'CI']);
  });

  it('should display repository cards when data is loaded', () => {
    fixture.detectChanges();
    const repoCards = fixture.debugElement.queryAll(By.css('.repo-card'));
    expect(repoCards.length).toBe(mockRepositories.length);
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
    
    const firstRepoCard = fixture.debugElement.queryAll(By.css('.repo-card'))[0];
    const ciBadgeRow = firstRepoCard.query(By.css('.badge-row:first-of-type'));
    expect(ciBadgeRow).toBeTruthy();
    
    const thirdRepoCard = fixture.debugElement.queryAll(By.css('.repo-card'))[2];
    const noBadgeRow = thirdRepoCard.query(By.css('.badge-row'));
    expect(noBadgeRow).toBeFalsy();
  });
});