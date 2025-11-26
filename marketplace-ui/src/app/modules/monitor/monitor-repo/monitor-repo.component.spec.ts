import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { MonitoringRepoComponent } from './monitor-repo.component';
import { Repository, TestResult } from '../github.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { By } from '@angular/platform-browser';
import {
  ASCENDING,
  DEFAULT_MODE,
  DESCENDING,
  FOCUSED_TAB,
  NAME_COLUMN,
  REPORT_MODE
} from '../../../shared/constants/common.constant';
import { SimpleChange, SimpleChanges } from '@angular/core';

describe('MonitoringRepoComponent', () => {
  let component: MonitoringRepoComponent;
  let fixture: ComponentFixture<MonitoringRepoComponent>;
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
            currentWorkflowState: 'deleted',
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

    await TestBed.configureTestingModule({
      imports: [
        MonitoringRepoComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
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

    fixture = TestBed.createComponent(MonitoringRepoComponent);
    component = fixture.componentInstance;
    const changes: SimpleChanges = {
      activeTab: new SimpleChange('focus', 'standard', false)
    };
    component.tabKey = FOCUSED_TAB;
    component.displayedRepositories = [...mockRepositories];
    component.ngOnChanges(changes);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize mode to default for given tabKey', () => {
    component.ngOnInit();
    expect(component.mode[FOCUSED_TAB]).toBe(DEFAULT_MODE);
  });

  it('should update criteria.search, reset page, update pageable, and call loadRepositories on search', () => {
    const searchString = 'asana';

    component.page = 5;
    component.pageSize = 20;
    component.criteria.pageable.page = 4;
    component.criteria.pageable.size = 20;

    spyOn(component, 'loadRepositories').and.callThrough();
    component.onSearchChanged(searchString);

    expect(component.page).toBe(1);
    expect(component.criteria.pageable.page).toBe(0);
    expect(component.criteria.pageable.size).toBe(component.pageSize);
    expect(component.criteria.search).toBe(searchString);
    expect(component.loadRepositories).toHaveBeenCalled();
  });

  it('should show all repositories when pageSize = -1', () => {
    component.pageSize = -1;
    component.displayedRepositories = [...mockRepositories];
    expect(component.displayedRepositories.length).toBe(
      mockRepositories.length
    );
  });

  it('should toggle sort direction if the same column is passed and call loadRepositories', () => {
    // Setup initial state
    component.sortColumn = NAME_COLUMN;
    component.sortDirection = ASCENDING;
    component.criteria.workflowType = NAME_COLUMN;

    spyOn(component, 'loadRepositories').and.callThrough();

    component.sortRepositoriesByColumn(NAME_COLUMN);

    expect(component.sortDirection).toBe(DESCENDING);
    expect(component.criteria.sortDirection).toBe(DESCENDING);
    expect(component.criteria.workflowType).toBe(NAME_COLUMN);
    expect(component.loadRepositories).toHaveBeenCalled();
  });

  it('should return correct market URL', () => {
    const url = component.getMarketUrl('repo1');
    expect(url).toContain(encodeURIComponent('repo1'));
  });

  it('should return correct workflow match', () => {
    const repo = mockRepositories[0];
    const match = component.findWorkflowMatch(repo, 'CI');
    expect(match).toBeTruthy();
    expect(match!.workflowType).toBe('CI');

    const noMatch = component.findWorkflowMatch(repo, 'E2E');
    expect(noMatch).toBeUndefined();
  });

  it('should toggle mode via ngModel binding', fakeAsync(() => {
    component.mode[FOCUSED_TAB] = DEFAULT_MODE;
    fixture.detectChanges();
    const reportRadio = fixture.debugElement.query(
      By.css('#report-mode-focused')
    ).nativeElement as HTMLInputElement;

    reportRadio.click();
    fixture.detectChanges();
    tick();
    expect(component.mode[FOCUSED_TAB]).toBe(REPORT_MODE);
  }));

  it('should display repository links correctly in template', () => {
    const repoLinks = fixture.debugElement.queryAll(By.css('#product-name'));

    expect(repoLinks.length).toBe(3);
    expect(repoLinks[0].nativeElement.textContent.trim()).toBe('repo1');
    expect(repoLinks[1].nativeElement.textContent.trim()).toBe('repo2');
    expect(repoLinks[2].nativeElement.textContent.trim()).toBe('repo3');
  });

  it('should show no-repositories message when filtered list is empty', () => {
    spyOn(component, 'loadRepositories').and.callFake(() => {
      component.displayedRepositories = [];
      component.totalElements = 0;
    });

    component.onSearchChanged('asanaaaaa');
    fixture.detectChanges();

    const noRepositoriesMessage = fixture.debugElement.query(
      By.css('.no-repositories')
    );
    expect(noRepositoriesMessage).toBeTruthy();
    expect(noRepositoriesMessage.nativeElement.textContent).toContain(
      'common.monitor.dashboard.noRepositories'
    );
  });

  it('should update sort icons correctly', () => {
    const header = fixture.debugElement.query(By.css('th h5.table-header'));
    expect(header.nativeElement.className).toContain('bi-arrow-up');

    component.sortRepositoriesByColumn(component.COLUMN_NAME);
    fixture.detectChanges();
    expect(header.nativeElement.className).toContain('bi-arrow-down');
  });

  it('should update page, pageable.page, pageable.size and call loadRepositories on page change', () => {
    // Arrange
    const newPage = 3;
    component.pageSize = 15;
    component.criteria.pageable.size = 10; // initial value
    spyOn(component, 'loadRepositories').and.callThrough();

    // Act
    component.onPageChange(newPage);

    // Assert
    expect(component.page).toBe(newPage);
    expect(component.criteria.pageable.page).toBe(newPage - 1);
    expect(component.criteria.pageable.size).toBe(component.pageSize);
    expect(component.loadRepositories).toHaveBeenCalled();
  });

  it('should update pageSize, reset page to 1, pageable.page to 0, pageable.size and call loadRepositories on page size change', () => {
    // Arrange
    const newSize = 25;
    component.page = 4;
    component.criteria.pageable.page = 3; // initial value
    spyOn(component, 'loadRepositories').and.callThrough();

    // Act
    component.onPageSizeChanged(newSize);

    // Assert
    expect(component.pageSize).toBe(newSize);
    expect(component.page).toBe(1);
    expect(component.criteria.pageable.page).toBe(0);
    expect(component.criteria.pageable.size).toBe(newSize);
    expect(component.loadRepositories).toHaveBeenCalled();
  });

  it('should set isFocused to true when activeTab is not STANDARD_TAB and call loadRepositories', () => {
    component.activeTab = 'not-standard';
    component.page = 2;
    component.pageSize = 15;
    spyOn(component, 'loadRepositories').and.callThrough();

    component.updateCriteriaAndLoad();

    expect(component.criteria.isFocused).toBe('true');
    expect(component.criteria.pageable.size).toBe(component.pageSize);
    expect(component.criteria.pageable.page).toBe(component.page - 1);
    expect(component.loadRepositories).toHaveBeenCalled();
  });

  it('should set isFocused to empty string when activeTab is STANDARD_TAB and call loadRepositories', () => {
    component.activeTab = 'standard'; // STANDARD_TAB
    component.page = 4;
    component.pageSize = 20;
    spyOn(component, 'loadRepositories').and.callThrough();

    component.updateCriteriaAndLoad();

    expect(component.criteria.isFocused).toBe('false');
    expect(component.criteria.pageable.size).toBe(component.pageSize);
    expect(component.criteria.pageable.page).toBe(component.page - 1);
    expect(component.loadRepositories).toHaveBeenCalled();
  });
});
