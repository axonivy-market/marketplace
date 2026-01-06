import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AdminAuthGuard } from './admin-auth.guard';
import { AdminDashboardService } from './admin-dashboard.service';
import { AdminAuthService } from './admin-auth.service';

describe('AdminAuthGuard', () => {
  let guard: AdminAuthGuard;
  let adminDashboardServiceMock: AdminDashboardService;
  let adminAuthServiceMock: jasmine.SpyObj<AdminAuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(() => {
    adminDashboardServiceMock = {} as unknown as AdminDashboardService;
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
    it('should return true when user is authenticated', () => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(true);

      const result = guard.canActivate();

      expect(result).toBe(true);
      expect(routerMock.navigate).not.toHaveBeenCalled();
    });

    it('should return false when user is not authenticated', () => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(false);

      const result = guard.canActivate();

      expect(result).toBe(false);
    });

    it('should navigate to request-access when user is not authenticated', () => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(false);

      guard.canActivate();

      expect(routerMock.navigate).toHaveBeenCalledWith(['request-access']);
    });

    it('should not navigate when user is authenticated', () => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(true);

      guard.canActivate();

      expect(routerMock.navigate).not.toHaveBeenCalled();
    });
  });
});
