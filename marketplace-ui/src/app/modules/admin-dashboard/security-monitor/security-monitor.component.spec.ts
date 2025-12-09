import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SecurityMonitorComponent } from './security-monitor.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { TIME_UNITS } from '../../../shared/constants/common.constant';
import { PageTitleService } from '../../../shared/services/page-title.service';

describe('SecurityMonitorComponent', () => {
  let component: SecurityMonitorComponent;
  let fixture: ComponentFixture<SecurityMonitorComponent>;
  let translateService: jasmine.SpyObj<TranslateService>;
  let pageTitleService: jasmine.SpyObj<PageTitleService>;

  beforeEach(async () => {
    const translateSpy = jasmine.createSpyObj('TranslateService', ['get']);
    const pageTitleSpy = jasmine.createSpyObj('TranslateService', [
      'setTitleOnLangChange'
    ]);

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule],
      providers: [
        { provide: TranslateService, useValue: translateSpy },
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
});
