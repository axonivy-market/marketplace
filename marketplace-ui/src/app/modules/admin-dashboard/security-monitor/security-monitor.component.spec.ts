import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SecurityMonitorComponent } from './security-monitor.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { TIME_UNITS } from '../../../shared/constants/common.constant';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { AdminDashboardService } from '../admin-dashboard.service';
import { of } from 'rxjs';

describe('SecurityMonitorComponent', () => {
  let component: SecurityMonitorComponent;
  let fixture: ComponentFixture<SecurityMonitorComponent>;

  beforeEach(async () => {
    const securityMonitorSpy = jasmine.createSpyObj('AdminDashboardService', [
      'getSecurityDetails'
    ]);
    const pageTitleSpy = jasmine.createSpyObj('PageTitleService', [
      'setTitleOnLangChange'
    ]);

    securityMonitorSpy.getSecurityDetails.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: AdminDashboardService, useValue: securityMonitorSpy },
        { provide: PageTitleService, useValue: pageTitleSpy }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityMonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
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
    const oneMinuteAgo = new Date(
      new Date().getTime() - 60 * 1000
    ).toISOString();
    TIME_UNITS[0] = { SECONDS: 60, SINGULAR: 'minute', PLURAL: 'minutes' };
    const result = component.formatCommitDate(oneMinuteAgo);
    expect(result).toBe('1 minute ago');
  });

  it('should return "1 year ago" for dates exactly 1 year ago', () => {
    const oneYearAgo = new Date(
      new Date().getTime() - 365 * 24 * 60 * 60 * 1000
    ).toISOString();
    const result = component.formatCommitDate(oneYearAgo);
    expect(result).toBe('1 year ago');
  });

  it('should return "2 years ago" for dates 2 years ago', () => {
    const twoYearsAgo = new Date(
      new Date().getTime() - 2 * 365 * 24 * 60 * 60 * 1000
    ).toISOString();
    const result = component.formatCommitDate(twoYearsAgo);
    expect(result).toBe('2 years ago');
  });

  it('should return empty string when TIME_UNITS is empty', () => {
    const originalUnits = [...TIME_UNITS];
    TIME_UNITS.length = 0;

    const oneYearAgo = new Date(
      new Date().getTime() - 365 * 24 * 60 * 60 * 1000
    ).toISOString();
    const result = component.formatCommitDate(oneYearAgo);
    
    expect(result).toBe('');

    TIME_UNITS.push(...originalUnits);
  });

  it('should clear session data', () => {
    spyOn(sessionStorage, 'removeItem');
    
    component['clearSessionData']();
    
    expect(sessionStorage.removeItem).toHaveBeenCalledWith('security-monitor-data');
  });

  it('should call clearSessionData when loadSessionData throws error', () => {
    spyOn(sessionStorage, 'getItem').and.throwError('Parse error');
    spyOn(sessionStorage, 'removeItem');
    
    component['loadSessionData']();
    
    expect(sessionStorage.removeItem).toHaveBeenCalledWith('security-monitor-data');
  });
});
