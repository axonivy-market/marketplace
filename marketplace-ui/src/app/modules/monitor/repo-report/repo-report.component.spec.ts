
import { RepoReportComponent } from './repo-report.component';
import { GithubService, TestStep } from '../github.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs'; // Ensure RxJS is imported correctly
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { By } from '@angular/platform-browser';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';

const mockTestSteps: TestStep[] = [
  { name: 'Step 1', status: 'PASSED', type: 'unit' },
  { name: 'Step 2', status: 'FAILED', type: 'integration' },
  { name: 'Step 3', status: 'SKIPPED', type: 'unit' }
];

describe('RepoReportComponent', () => {
  let component: RepoReportComponent;
  let fixture: ComponentFixture<RepoReportComponent>;
  let githubServiceSpy: jasmine.SpyObj<GithubService>;
  let activatedRouteStub: any;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    githubServiceSpy = jasmine.createSpyObj('GithubService', ['getTestReport']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    activatedRouteStub = {
      snapshot: {
        paramMap: {
          get: (key: string) => {
            if (key === 'repo') return 'repo1';
            if (key === 'workflow') return 'CI';
            return null;
          }
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [RepoReportComponent, TranslateModule.forRoot(), MatomoTestingModule.forRoot()],
      providers: [
        { provide: GithubService, useValue: githubServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: Router, useValue: routerSpy },
        LanguageService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RepoReportComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle missing repo or workflow params', () => {
    activatedRouteStub.snapshot.paramMap.get = () => null;
    component.ngOnInit();
    expect(component.errorMessage).toBe('Missing repository or workflow name');
    expect(component.report.length).toBe(0);
  });

  it('should handle error from getTestReport', () => {
    githubServiceSpy.getTestReport.and.returnValue(throwError(() => new Error('API error')));
    component.fetchTestReport('repo1', 'CI');
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Failed to load test report');
  });

  it('should handle non-array response from getTestReport', () => {
    githubServiceSpy.getTestReport.and.returnValue(of(mockTestSteps[0]));
    component.fetchTestReport('repo1', 'CI');
    expect(component.report.length).toBe(1);
    expect(component.report[0].name).toBe('Step 1');
  });

  it('should render test steps when report is available', () => {
    githubServiceSpy.getTestReport.and.returnValue(of(mockTestSteps as any));
    component.ngOnInit();
    fixture.detectChanges();
    const steps = fixture.debugElement.queryAll(By.css('.test-step'));
    expect(steps.length).toBe(3);
    expect(steps[0].nativeElement.textContent).toContain('Step 1');
    expect(steps[1].nativeElement.textContent).toContain('Step 2');
    expect(steps[2].nativeElement.textContent).toContain('Step 3');
  });

  it('should set loading to true while fetching', () => {
    githubServiceSpy.getTestReport.and.returnValue(of([] as any));
    component.fetchTestReport('repo1', 'CI');
    expect(component.loading).toBeFalse();
  });

  it('should handle ngOnInit with empty report', () => {
    githubServiceSpy.getTestReport.and.returnValue(of([] as any));
    component.ngOnInit();
    expect(component.report.length).toBe(0);
    expect(component.errorMessage).toBe('');
  });

  it('should navigate back to monitoring page when backToMonitorPage is called', () => {
    component.backToMonitorPage();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/monitoring']);
  });
});
