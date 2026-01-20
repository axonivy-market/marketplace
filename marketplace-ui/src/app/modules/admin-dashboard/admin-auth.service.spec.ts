import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminAuthService } from './admin-auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { ADMIN_SESSION_TOKEN, BEARER } from '../../shared/constants/common.constant';

describe('AdminAuthService', () => {
  let service: AdminAuthService;
  let httpTestingController: HttpTestingController;
  let sessionStorageMock: { getItem: jasmine.Spy; setItem: jasmine.Spy; removeItem: jasmine.Spy };
  let sessionStorageRef: SessionStorageRef;

  beforeEach(() => {
    sessionStorageMock = {
      getItem: jasmine.createSpy('getItem'),
      setItem: jasmine.createSpy('setItem'),
      removeItem: jasmine.createSpy('removeItem')
    };

    sessionStorageRef = {
      session: sessionStorageMock,
      platformId: 'browser' 
    } as any;    

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminAuthService,
        { provide: SessionStorageRef, useValue: sessionStorageRef }
      ]
    });

    service = TestBed.inject(AdminAuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('token', () => {
    it('should return token from sessionStorage', () => {
      sessionStorageMock.getItem.and.returnValue('test-token');

      const token = service.token;

      expect(token).toBe('test-token');
      expect(sessionStorageMock.getItem).toHaveBeenCalledWith(ADMIN_SESSION_TOKEN);
    });

    it('should return null when no token exists', () => {
      sessionStorageMock.getItem.and.returnValue(null);

      const token = service.token;

      expect(token).toBeNull();
    });

    it('should return null when sessionStorage is not available', () => {
      const nullStorageRef = { session: null };
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          AdminAuthService,
          { provide: SessionStorageRef, useValue: nullStorageRef }
        ]
      });
      const nullService = TestBed.inject(AdminAuthService);
      const nullHttpTestingController = TestBed.inject(HttpTestingController);

      const token = nullService.token;

      expect(token).toBeNull();
      nullHttpTestingController.verify();
    });
  });

  describe('setToken', () => {
    it('should save token to sessionStorage', () => {
      service.setToken('new-token');

      expect(sessionStorageMock.setItem).toHaveBeenCalledWith(ADMIN_SESSION_TOKEN, 'new-token');
    });

    it('should not throw when sessionStorage is not available', () => {
      const nullStorageRef = { session: null };
      
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          AdminAuthService,
          { provide: SessionStorageRef, useValue: nullStorageRef }
        ]
      });
      const nullService = TestBed.inject(AdminAuthService);
      const nullHttpTestingController = TestBed.inject(HttpTestingController);

      expect(() => nullService.setToken('new-token')).not.toThrow();
      nullHttpTestingController.verify();
    });

    it('should remove token from sessionStorage', () => {
      service.clearToken();

      expect(sessionStorageMock.removeItem).toHaveBeenCalledWith(ADMIN_SESSION_TOKEN);
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when token exists', (done) => {
      sessionStorageMock.getItem.and.returnValue('test-token');

      service.isAuthenticated().subscribe(result => {
        expect(result).toBe(true);
        done();
      });

      const req = httpTestingController.expectOne(request => 
        request.url.includes('/github/validate-token')
      );
      expect(req.request.method).toBe('PUT');
      req.flush(true);
    });

    it('should return headers with Authorization when token exists', () => {
      sessionStorageMock.getItem.and.returnValue('test-token');

      const headers = service.getAuthHeaders();

      expect(headers.get('Authorization')).toBe(`${BEARER} test-token`);
    });

    it('should return empty headers when token is null', () => {
      sessionStorageMock.getItem.and.returnValue(null);

      const headers = service.getAuthHeaders();

      expect(headers.get('Authorization')).toBeNull();
      expect(headers.keys().length).toBe(0);
    });
  });
});
