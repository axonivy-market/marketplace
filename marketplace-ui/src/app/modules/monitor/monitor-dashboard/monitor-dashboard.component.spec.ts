import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { of } from 'rxjs';
import { PLATFORM_ID } from '@angular/core';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
class MockPageTitleService {
  setTitleOnLangChange = jasmine.createSpy('setTitleOnLangChange');
}

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;
  let mockPageTitleService: MockPageTitleService;

  beforeEach(async () => {
    mockPageTitleService = new MockPageTitleService();
    // mockRepositories = [
    //   {
    //     repoName: 'repo1',
    //     productId: 'id1',
    //     htmlUrl: 'https://github.com/user/repo1',
    //     workflowInformation: [
    //       {
    //         workflowType: 'CI',
    //         lastBuilt: new Date('2025-07-20T12:00:00Z'),
    //         conclusion: 'success',
    //         lastBuiltRunUrl:
    //           'https://github.com/market/rtf-factory/actions/runs/11111',
    //         currentWorkflowState: 'active',
    //         disabledDate: null
    //
    //       },
    //       {
    //         workflowType: 'DEV',
    //         lastBuilt: new Date('2025-07-21T12:00:00Z'),
    //         conclusion: 'failure',
    //         lastBuiltRunUrl:
    //           'https://github.com/market/rtf-factory/actions/runs/11111',
    //         currentWorkflowState: 'disabled_manually',
    //         disabledDate: new Date('2025-07-22T12:00:00Z')
    //       }
    //     ],
    //     focused: true,
    //     testResults: [
    //       {
    //         workflow: 'CI',
    //         results: { PASSED: 20 }
    //       } as TestResult
    //     ]
    //   },
    //   {
    //     repoName: 'repo2',
    //     productId: 'id2',
    //     htmlUrl: 'https://github.com/user/repo2',
    //     workflowInformation: [],
    //     focused: false,
    //     testResults: []
    //   },
    //   {
    //     repoName: 'repo3',
    //     productId: 'id3',
    //     htmlUrl: 'https://github.com/user/repo3',
    //     workflowInformation: [],
    //     focused: false,
    //     testResults: []
    //   }
    // ];

    const githubServiceSpy = jasmine.createSpyObj('GithubService', [
      'getRepositories'
    ]);
    const pageTitleServiceSpy = jasmine.createSpyObj(PageTitleService, [
      'setTitleOnLangChange'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        MonitoringDashboardComponent,
        TranslateModule.forRoot(),
        RouterModule.forRoot([]),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: PageTitleService, useValue: mockPageTitleService },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        },
        TranslateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit (platform: browser)', () => {
    it('should call setTitleOnLangChange', () => {
      spyOn(component['route'].queryParams, 'subscribe').and.callThrough();

      component.ngOnInit();

      expect(mockPageTitleService.setTitleOnLangChange).toHaveBeenCalledWith(
        'common.monitor.dashboard.pageTitle'
      );
      expect(component['route'].queryParams.subscribe).toHaveBeenCalled();
    });

    it('should set initialFilter and activeTab when query param "search" exists', () => {
      const route = TestBed.inject(ActivatedRoute);
      (route as any).queryParams = of({ search: 'test-repo' });

      component.ngOnInit();

      expect(component.initialFilter()).toBe('test-repo');
      expect(component.activeTab).toBe(component.STANDARD_TAB);
    });
  });

  it('should change active tab when setActiveTab() is called', () => {
    component.activeTab = component.FOCUSED_TAB;

    component.setActiveTab(component.STANDARD_TAB);

    expect(component.activeTab).toBe(component.STANDARD_TAB);
  });
});
