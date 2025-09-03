import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringRepoComponent } from './monitor-repo.component';
import { GithubService, Repository, TestResult } from '../github.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';

describe('MonitoringRepoComponent', () => {
  let component: MonitoringRepoComponent;
  let fixture: ComponentFixture<MonitoringRepoComponent>;
  let githubService: jasmine.SpyObj<GithubService>;
  let router: jasmine.SpyObj<Router>;
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

    await TestBed.configureTestingModule({
      imports: [
        MonitoringRepoComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
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

    fixture = TestBed.createComponent(MonitoringRepoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


});
