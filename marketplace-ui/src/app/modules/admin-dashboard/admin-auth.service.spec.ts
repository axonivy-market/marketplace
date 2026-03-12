import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminAuthService, JwtDTO } from './admin-auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { ADMIN_SESSION_TOKEN, BEARER, GITHUB_USER } from '../../shared/constants/common.constant';
import { GitHubUser } from '../../auth/auth.service';
import { PLATFORM_ID } from '@angular/core';
import { ForwardingError } from '../../core/interceptors/api.interceptor';
import { API_URI } from '../../shared/constants/api.constant';

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

  describe('AdminAuthService', () => {
    it('should set adminInfo from sessionStorage in constructor when user exists', () => {
      const mockUser: GitHubUser = {
        login: 'octocat',
        name: 'The Octocat',
        avatarUrl: 'https://avatar.url',
        url: 'https://github.com/octocat'
      };

      sessionStorageMock.getItem.and.callFake((key: string) => {
        if (key === GITHUB_USER) {
          return JSON.stringify(mockUser);
        }
        return null;
      });

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          AdminAuthService,
          { provide: SessionStorageRef, useValue: sessionStorageRef },
          { provide: PLATFORM_ID, useValue: 'browser' }
        ]
      });

      const recreatedService = TestBed.inject(AdminAuthService);

      expect(recreatedService.adminInfo()).toEqual(mockUser);
      expect(sessionStorageMock.getItem).toHaveBeenCalledWith(GITHUB_USER);
    });

    it('should store user via SessionStorageRef and update adminInfo signal', () => {
      const mockUser: GitHubUser = {
        login: 'octocat',
        name: 'The Octocat',
        avatarUrl: 'https://avatar.url',
        url: 'https://github.com/octocat'
      };

      service.setUser(mockUser);

      expect(sessionStorageMock.setItem).toHaveBeenCalledWith(
        GITHUB_USER,
        JSON.stringify(mockUser)
      );

      expect(service.adminInfo()).toEqual(mockUser);
    });

    it('should remove user and token via SessionStorageRef and clear adminInfo', () => {
      const mockUser: GitHubUser = {
        login: 'octocat',
        name: 'The Octocat',
        avatarUrl: 'https://avatar.url',
        url: 'https://github.com/octocat'
      };
      service.setUser(mockUser);

      service.logout();

      expect(sessionStorageMock.removeItem).toHaveBeenCalledWith(GITHUB_USER);
      expect(sessionStorageMock.removeItem).toHaveBeenCalledWith(ADMIN_SESSION_TOKEN);

      expect(service.adminInfo()).toBeNull();
    });
  });
  
  describe('requestAccessToken', () => {
    it('should clear token and call POST with correct payload and context', () => {
      const testToken = 'github-oauth-token';

      const jwtResponse: JwtDTO = {
        token: 'jwt-token',
        user: {
          login: 'octocat',
          name: 'The Octocat',
          avatarUrl: 'https://avatar.url',
          url: 'https://github.com/octocat'
        }
      };

      const setTokenSpy = spyOn(service, 'setToken').and.callThrough();

      service.requestAccessToken(testToken).subscribe(response => {
        expect(response).toEqual(jwtResponse);
      });

      expect(setTokenSpy).toHaveBeenCalledWith('');

      const req = httpTestingController.expectOne(API_URI.GITHUB_REQUEST_ACCESS);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ token: testToken });
      expect(req.request.context.get(ForwardingError)).toBeTrue();

      req.flush(jwtResponse);
    });
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
