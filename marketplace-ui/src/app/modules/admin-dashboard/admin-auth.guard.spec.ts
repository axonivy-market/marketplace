import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AdminAuthGuard } from './admin-auth.guard';
import { AdminDashboardService } from './admin-dashboard.service';
import { AdminAuthService } from './admin-auth.service';

describe('AdminAuthGuard', () => {
  let guard: AdminAuthGuard;
  let adminDashboardServiceMock: AdminDashboardService;
  let adminAuthServiceMock: jasmine.SpyObj<AdminAuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(() => {
    adminDashboardServiceMock = jasmine.createSpyObj('AdminDashboardService', ['methodName']);
    adminAuthServiceMock = jasmine.createSpyObj('AdminAuthService', [
      'isAuthenticated'
    ]);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AdminAuthGuard,
        { provide: AdminDashboardService, useValue: adminDashboardServiceMock },
        { provide: AdminAuthService, useValue: adminAuthServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    });

    guard = TestBed.inject(AdminAuthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  describe('canActivate', () => {
    it('should return true when user is authenticated', (done) => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(of(true));

      guard.canActivate().subscribe(result => {
        expect(result).toBe(true);
        expect(routerMock.navigate).not.toHaveBeenCalled();
        done();
      });
    });

    it('should return false when user is not authenticated', (done) => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(of(false));

      guard.canActivate().subscribe(result => {
        expect(result).toBe(false);
        done();
      });
    });

    it('should navigate to request-access when user is not authenticated', (done) => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(of(false));

      guard.canActivate().subscribe(() => {
        expect(routerMock.navigate).toHaveBeenCalledWith(['request-access']);
        done();
      });
    });

    it('should not navigate when user is authenticated', (done) => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(of(true));

      guard.canActivate().subscribe(() => {
        expect(routerMock.navigate).not.toHaveBeenCalled();
        done();
      });
    });
  });
});
