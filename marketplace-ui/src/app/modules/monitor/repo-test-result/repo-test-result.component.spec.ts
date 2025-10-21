import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RepoTestResultComponent } from './repo-test-result.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { of } from 'rxjs';
import { Repository, TestSummary } from '../github.service';
import { By } from '@angular/platform-browser';

describe('RepoTestResultComponent', () => {
  let component: RepoTestResultComponent;
  let fixture: ComponentFixture<RepoTestResultComponent>;
  let mockTranslateService: jasmine.SpyObj<TranslateService>;
  let router: jasmine.SpyObj<Router>;
  let mockRepository: Repository;

  beforeEach(async () => {
    mockTranslateService = jasmine.createSpyObj('TranslateService', [
      'instant', 'get'
    ]);
    mockTranslateService.instant.and.callFake((key: string) => key);
    mockTranslateService.get.and.callFake((key: string) => of(key));
    mockRepository = {
    repoName: 'test-repo',
    htmlUrl: 'https://github.com/user/test-repo',
    workflowInformation: [
      {
        workflowType: 'CI',
        lastBuilt: new Date('2025-08-01T12:00:00Z'),
        conclusion: 'success',
        lastBuiltRunUrl: 'https://github.com/user/test-repo/actions/runs/123'
      }
    ],
    testResults: [
      {
        workflow: 'CI',
        results: { PASSED: 10, FAILED: 2 }
      }
    ]
  } as Repository;

    await TestBed.configureTestingModule({
      imports: [RepoTestResultComponent, MatomoTestingModule.forRoot(), TranslateModule.forRoot(),],
      providers: [
        { provide: TranslateService, useValue: mockTranslateService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {},
            params: of({}),
            queryParams: of({}),
            data: of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RepoTestResultComponent);
    component = fixture.componentInstance;

    component.repository = mockRepository;
    component.workflowInfo = mockRepository.workflowInformation[0];
    component.workflowType = 'CI';
    component.mode = 'default';

    router = jasmine.createSpyObj('Router', ['navigate']);
    component.router = router;
    fixture.detectChanges();
  });

  afterEach(() => {
    mockTranslateService.instant.calls.reset();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render badge image for workflow', () => {
    const badgeImg = fixture.debugElement.query(By.css('.badge-image'));
    expect(badgeImg).toBeTruthy();
    expect(badgeImg.nativeElement.src).toContain('pass-badge.svg');
    expect(badgeImg.nativeElement.alt).toBe('Pass badge');
  });

  it('should display last built date', () => {
    const dateSpan = fixture.debugElement.query(By.css('.built-date'));
    expect(dateSpan).toBeTruthy();
    expect(dateSpan.nativeElement.textContent).toContain('8/1/25');
  });

  it('should open new tab on badge click in default mode', () => {
    spyOn(window, 'open');
    component.onBadgeClick(mockRepository.repoName, 'CI', 'default');
    expect(window.open).toHaveBeenCalledWith(
      'https://github.com/user/test-repo/actions/runs/123',
      '_blank'
    );
  });

  it('should navigate to report page on badge click in report mode', () => {
    component.mode = 'report';
    component.onBadgeClick(mockRepository.repoName, 'CI', 'report');
    expect(router.navigate).toHaveBeenCalledWith(['/monitoring', 'test-repo', 'CI']);
  });

  it('should not render badge if workflowInfo is missing', () => {
    component.workflowInfo = undefined;
    fixture.detectChanges();
    const badgeImg = fixture.debugElement.query(By.css('.badge-image'));
    expect(badgeImg).toBeNull();
  });

  it('should handle empty test results gracefully', () => {
  component.mode = 'report';

  const emptyResults: TestSummary = {
    FAILED: 0,
    PASSED: 0,
    SKIPPED: 0
  };

  component.repository.testResults = [{ workflow: 'CI', results: emptyResults }];
  fixture.detectChanges();

  const subStatus = fixture.debugElement.query(By.css('.sub-status'));
  expect(subStatus).toBeTruthy();
  expect(subStatus.nativeElement.textContent.trim()).toBe('');
});

  it('should correctly compute conclusion key', () => {
    expect(component.getConclusionKey('SUCCESS')).toBe('success');
    expect(component.getConclusionKey(undefined)).toBe('');
  });

  it('should return empty fields for empty state', () => {
    const result = component.getWorkflowDisplay('');
    expect(result.icon).toBe('');
    expect(result.label).toBe('');
    expect(result.tooltip).toBe('');
  });

  it('should return warning icon for state containing disabled', () => {
    const state = 'disabled_inactivity';
    const result = component.getWorkflowDisplay(state);
    expect(result.icon).toBe('⚠️');
    expect(result.label).toBe('common.monitor.workflow.status.' + state);
    expect(result.tooltip).toBe('common.monitor.workflow.tooltip.' + state);
  });

  it('should return warning icon for simple disabled', () => {
    const state = 'disabled_manually';
    const result = component.getWorkflowDisplay(state);
    expect(result.icon).toBe('⚠️');
  });

  it('should return delete icon for deleted state (precedence over disabled substring)', () => {
    const state = 'deleted';
    const result = component.getWorkflowDisplay(state);
    expect(result.icon).toBe('❌');
    expect(result.label).toBe('common.monitor.workflow.status.' + state);
  });

  it('should return green icon for active state', () => {
    const state = 'active';
    const result = component.getWorkflowDisplay(state);
    expect(result.icon).toBe('🟢');
    expect(result.label).toBe('common.monitor.workflow.status.' + state);
    expect(result.tooltip).toBe('common.monitor.workflow.tooltip.' + state);
  });

  it('should report hasAnyTestResults and hasWorkflowTestResults true for matching non-empty workflow', () => {
    component.mode = 'report';
    component.workflowInfo = { workflowType: 'CI' } as any;
    component.repository.testResults = [
      { workflow: 'CI', results: { PASSED: 5, FAILED: 2, SKIPPED: 1 } }
    ];
    expect(component.hasAnyTestResults()).toBeTrue();
    expect(component.hasWorkflowTestResults()).toBeTrue();
  });

  it('should report hasAnyTestResults true but hasWorkflowTestResults false for different workflow', () => {
    component.mode = 'report';
    component.workflowInfo = { workflowType: 'CD' } as any;
    component.repository.testResults = [
      { workflow: 'CI', results: { PASSED: 3, FAILED: 0, SKIPPED: 0 } }
    ];
    expect(component.hasAnyTestResults()).toBeTrue();
    expect(component.hasWorkflowTestResults()).toBeFalse();
  });

  it('should report both false when results are null/undefined', () => {
    component.mode = 'report';
    component.workflowInfo = { workflowType: 'CI' } as any;
    component.repository.testResults = [
      { workflow: 'CI', results: null as any },
      { workflow: 'CI', results: undefined as any }
    ];
    expect(component.hasAnyTestResults()).toBeFalse();
    expect(component.hasWorkflowTestResults()).toBeFalse();
  });
});
