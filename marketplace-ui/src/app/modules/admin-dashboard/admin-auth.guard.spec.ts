import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { PLATFORM_ID } from '@angular/core';
import { of, throwError } from 'rxjs';
import { AdminAuthGuard, REQUEST_ACCESS_PATH } from './admin-auth.guard';
import { AdminAuthService } from './admin-auth.service';

describe('AdminAuthGuard', () => {
  let guard: AdminAuthGuard;
  let adminAuthServiceMock: jasmine.SpyObj<AdminAuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(() => {
    adminAuthServiceMock = jasmine.createSpyObj('AdminAuthService', ['isAuthenticated']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AdminAuthGuard,
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
        expect(routerMock.navigate).toHaveBeenCalledWith([REQUEST_ACCESS_PATH]);
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

    it('should return false when not running in browser platform', (done) => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          AdminAuthGuard,
          { provide: AdminAuthService, useValue: adminAuthServiceMock },
          { provide: Router, useValue: routerMock },
          { provide: PLATFORM_ID, useValue: 'server' }
        ]
      });
      const serverGuard = TestBed.inject(AdminAuthGuard);

      serverGuard.canActivate().subscribe(result => {
        expect(result).toBe(false);
        expect(adminAuthServiceMock.isAuthenticated).not.toHaveBeenCalled();
        expect(routerMock.navigate).not.toHaveBeenCalled();
        done();
      });
    });

    it('should handle authentication errors and navigate to request-access', (done) => {
      const error = new Error('Network error');
      adminAuthServiceMock.isAuthenticated.and.returnValue(throwError(() => error));

      guard.canActivate().subscribe(result => {
        expect(result).toBe(false);
        expect(routerMock.navigate).toHaveBeenCalledWith([REQUEST_ACCESS_PATH]);
        done();
      });
    });

    it('should return false when authentication check throws an error', (done) => {
      adminAuthServiceMock.isAuthenticated.and.returnValue(throwError(() => new Error('Auth failed')));

      guard.canActivate().subscribe(result => {
        expect(result).toBe(false);
        done();
      });
    });
  });
});
