import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SecurityMonitorComponent } from './security-monitor.component';
import { SecurityMonitorService } from './security-monitor.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { TIME_UNITS } from '../../shared/constants/common.constant';

describe('SecurityMonitorComponent', () => {
  let component: SecurityMonitorComponent;
  let fixture: ComponentFixture<SecurityMonitorComponent>;
  let securityMonitorService: jasmine.SpyObj<SecurityMonitorService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SecurityMonitorService', ['getSecurityDetails']);

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule],
      providers: [
        { provide: SecurityMonitorService, useValue: spy },
        { provide: TranslateService, useValue: spy }
      ],
    }).compileComponents();

    securityMonitorService = TestBed.inject(SecurityMonitorService) as jasmine.SpyObj<SecurityMonitorService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityMonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should show an error message when token is empty and onSubmit is called', () => {
    component.token = '';
    component.onSubmit();
    expect(component.errorMessage).toBe('Token is required');
  });

  it('should call SecurityMonitorService and display repos when token is valid and response is successful', () => {
    const mockRepos: ProductSecurityInfo[] = [
      {
        repoName: 'repo1',
        visibility: 'public',
        archived: false,
        dependabot: { status: 'ENABLED', alerts: {} },
        codeScanning: { status: 'ENABLED', alerts: {} },
        secretScanning: { status: 'ENABLED', numberOfAlerts: 0 },
        branchProtectionEnabled: true,
        lastCommitSHA: '12345',
        lastCommitDate: '',
      },
    ];

    securityMonitorService.getSecurityDetails.and.returnValue(of(mockRepos));

    component.token = 'valid-token';
    component.onSubmit();

    fixture.detectChanges();

    expect(securityMonitorService.getSecurityDetails).toHaveBeenCalledWith('valid-token');
    expect(component.repos).toEqual(mockRepos);
    expect(component.isAuthenticated).toBeTrue();

    const repoCards = fixture.debugElement.queryAll(By.css('.repo-card'));
    expect(repoCards.length).toBe(mockRepos.length);
    expect(repoCards[0].nativeElement.querySelector('h3').textContent).toBe('repo1');
  });

  it('should handle 401 Unauthorized error correctly', () => {
    const mockError = new HttpErrorResponse({ status: 401 });

    securityMonitorService.getSecurityDetails.and.returnValue(throwError(() => mockError));

    component.token = 'invalid-token';
    component.onSubmit();

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Unauthorized access.');
  });

  it('should handle generic error correctly', () => {
    const mockError = new HttpErrorResponse({ status: 500 });

    securityMonitorService.getSecurityDetails.and.returnValue(throwError(() => mockError));

    component.token = 'invalid-token';
    component.onSubmit();

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Failed to fetch security data. Check logs for details.');
  });

  it('should navigate to the correct URL for a repo page', () => {
    spyOn(window, 'open');
    component.navigateToRepoPage('example-repo', 'secretScanning');
    expect(window.open).toHaveBeenCalledWith(
      'https://github.com/axonivy-market/example-repo/security/secret-scanning',
      '_blank'
    );

    component.navigateToRepoPage('example-repo', 'lastCommit', 'abc123');
    expect(window.open).toHaveBeenCalledWith(
      'https://github.com/axonivy-market/example-repo/commit/abc123',
      '_blank'
    );
  });

  it('should handle empty alerts correctly in hasAlerts', () => {
    expect(component.hasAlerts({})).toBeFalse();
    expect(component.hasAlerts({ alert1: 1 })).toBeTrue();
  });

  it('should return correct alert keys from alertKeys', () => {
    const alerts = { alert1: 1, alert2: 2 };
    expect(component.alertKeys(alerts)).toEqual(['alert1', 'alert2']);
  });

  it('should return "just now" for dates less than 60 seconds ago', () => {
    const recentDate = new Date(new Date().getTime() - 30 * 1000).toISOString();
    const result = component.formatCommitDate(recentDate);
    expect(result).toBe('just now');
  });

  it('should return "1 minute ago" for dates 1 minute ago', () => {
    const oneMinuteAgo = new Date(new Date().getTime() - 60 * 1000).toISOString();
    TIME_UNITS[0] = { SECONDS: 60, SINGULAR: 'minute', PLURAL: 'minutes' };
    const result = component.formatCommitDate(oneMinuteAgo);
    expect(result).toBe('1 minute ago');
  });
});