import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { PLATFORM_ID } from '@angular/core';
import { of, throwError } from 'rxjs';
import { AdminAuthGuard, REQUEST_ACCESS_PATH } from './admin-auth.guard';
import { AdminAuthService } from './admin-auth.service';

describe('AdminAuthGuard', () => {
  let guard: AdminAuthGuard;
  let adminAuthServiceMock: MockedObject<AdminAuthService>;
  let routerMock: MockedObject<Router>;

  beforeEach(() => {
    adminAuthServiceMock = {
      isAuthenticated: vi.fn().mockName('AdminAuthService.isAuthenticated')
    } as any;
    routerMock = {
      navigate: vi.fn().mockName('Router.navigate')
    } as any;

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
    it('should return true when user is authenticated', async () => {
      adminAuthServiceMock.isAuthenticated.mockReturnValue(of(true));

      guard.canActivate().subscribe(result => {
        expect(result).toBe(true);
        expect(routerMock.navigate).not.toHaveBeenCalled();
      });
    });

    it('should return false when user is not authenticated', async () => {
      adminAuthServiceMock.isAuthenticated.mockReturnValue(of(false));

      guard.canActivate().subscribe(result => {
        expect(result).toBe(false);
      });
    });

    it('should navigate to request-access when user is not authenticated', async () => {
      adminAuthServiceMock.isAuthenticated.mockReturnValue(of(false));

      guard.canActivate().subscribe(() => {
        expect(routerMock.navigate).toHaveBeenCalledWith([REQUEST_ACCESS_PATH]);
      });
    });

    it('should not navigate when user is authenticated', async () => {
      adminAuthServiceMock.isAuthenticated.mockReturnValue(of(true));

      guard.canActivate().subscribe(() => {
        expect(routerMock.navigate).not.toHaveBeenCalled();
      });
    });

    it('should return false when not running in browser platform', async () => {
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
      });
    });

    it('should handle authentication errors and navigate to request-access', async () => {
      const error = new Error('Network error');
      adminAuthServiceMock.isAuthenticated.mockReturnValue(
        throwError(() => error)
      );

      guard.canActivate().subscribe(result => {
        expect(result).toBe(false);
        expect(routerMock.navigate).toHaveBeenCalledWith([REQUEST_ACCESS_PATH]);
      });
    });

    it('should return false when authentication check throws an error', async () => {
      adminAuthServiceMock.isAuthenticated.mockReturnValue(
        throwError(() => new Error('Auth failed'))
      );

      guard.canActivate().subscribe(result => {
        expect(result).toBe(false);
      });
    });
  });
});
