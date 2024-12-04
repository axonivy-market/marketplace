import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SecurityMonitorComponent } from './security-monitor.component';
import { SecurityMonitorService } from './security-monitor.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';

describe('SecurityMonitorComponent', () => {
  let component: SecurityMonitorComponent;
  let fixture: ComponentFixture<SecurityMonitorComponent>;
  let securityMonitorService: jasmine.SpyObj<SecurityMonitorService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SecurityMonitorService', ['getSecurityDetails']);

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule],
      providers: [
        { provide: SecurityMonitorService, useValue: spy }
      ]
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

  it('should show error message when token is empty and onSubmit is called', () => {
    component.token = '';
    component.onSubmit();
    expect(component.errorMessage).toBe('Token is required');
  });

  it('should call SecurityMonitorService and display repos when token is valid and response is successful', () => {
    const mockRepos = [
      { repoName: 'repo1', visibility: 'public', archived: false, dependabot: { status: 'ACTIVE', alerts: {} }, codeScanning: { status: 'ENABLED', alerts: {} }, secretsScanning: { status: 'ENABLED', numberOfAlerts: 0 }, branchProtectionEnabled: true, lastCommitSHA: '12345', lastCommitDate: '2024-12-04' },
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

  it('should handle error when token is invalid (401 Unauthorized)', () => {
    const mockError = { status: 401 };
    
    securityMonitorService.getSecurityDetails.and.returnValue(throwError(() => mockError));

    component.token = 'invalid-token';
    component.onSubmit();

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Unauthorized access. (The token should contain the "org:read" scope for authentication)');
    expect(component.isLoading).toBeFalse();
  });

  it('should handle generic error when fetching security data fails', () => {
    const mockError = { status: 500 };

    securityMonitorService.getSecurityDetails.and.returnValue(throwError(() => mockError));

    component.token = 'invalid-token';
    component.onSubmit();

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Failed to fetch security data. Check logs for details.');
    expect(component.isLoading).toBeFalse();
  });
});
