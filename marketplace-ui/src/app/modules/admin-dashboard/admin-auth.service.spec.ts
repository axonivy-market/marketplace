import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UserInfo } from '../../auth/auth.service';
import { ForwardingError } from '../../core/interceptors/api.interceptor';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import {
  ADMIN_SESSION_TOKEN,
  BEARER,
  GITHUB_USER
} from '../../shared/constants/common.constant';
import { AdminAuthService } from './admin-auth.service';

const mockUser: UserInfo = {
  login: 'octocat',
  name: 'The Octocat',
  avatarUrl: 'https://avatar.url',
  url: 'https://github.com/octocat',
  token: 'test-token'
};

const response: UserInfo = {
  login: 'octocat',
  name: 'The Octocat',
  avatarUrl: 'https://avatar.url',
  url: 'https://github.com/octocat',
  token: 'test-token'
};

describe('AdminAuthService', () => {
  let service: AdminAuthService;
  let httpTestingController: HttpTestingController;
  let sessionStorageMock: {
    getItem: jasmine.Spy;
    setItem: jasmine.Spy;
    removeItem: jasmine.Spy;
  };
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
    it('should set userInfo from sessionStorage in constructor when user exists', () => {
      sessionStorageMock.getItem.and.callFake((key: string) => {
        if (key === ADMIN_SESSION_TOKEN) {
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

      expect(recreatedService.userInfo()).toEqual(mockUser);
      expect(sessionStorageMock.getItem).toHaveBeenCalledWith(
        ADMIN_SESSION_TOKEN
      );
    });

    it('should store user via SessionStorageRef and update userInfo signal', () => {
      service.setUserInfo(mockUser);
      expect(sessionStorageMock.setItem).toHaveBeenCalledWith(
        ADMIN_SESSION_TOKEN,
        JSON.stringify(mockUser)
      );

      expect(service.userInfo()).toEqual(mockUser);
    });

    it('should remove token via SessionStorageRef and clear userInfo', () => {
      service.setUserInfo(mockUser);
      service.logout();

      expect(sessionStorageMock.removeItem).toHaveBeenCalledWith(
        ADMIN_SESSION_TOKEN
      );
      expect(service.userInfo()).toBeNull();
    });
  });

  describe('requestAccessToken', () => {
    it('should clear token and call POST with correct payload and context', () => {
      const testToken = 'github-oauth-token';
      const clearTokenSpy = spyOn(service, 'clearToken').and.callThrough();

      service.requestAccessToken(testToken).subscribe(response => {
        expect(response).toEqual(mockUser);
      });

      expect(clearTokenSpy).toHaveBeenCalled();

      const req = httpTestingController.expectOne(
        API_URI.GITHUB_REQUEST_ACCESS
      );

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ token: testToken });
      expect(req.request.context.get(ForwardingError)).toBeTrue();

      req.flush(mockUser);
    });
  });

  describe('token', () => {
    it('should return token from sessionStorage', () => {
      sessionStorageMock.getItem.and.returnValue(JSON.stringify(mockUser));

      const token = service.token;

      expect(token).toBe(mockUser.token);
      expect(sessionStorageMock.getItem).toHaveBeenCalledWith(
        ADMIN_SESSION_TOKEN
      );
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

  // describe('setToken', () => {
  //   it('should save token to sessionStorage', () => {
  //     service.setToken('new-token');

  //     expect(sessionStorageMock.setItem).toHaveBeenCalledWith(ADMIN_SESSION_TOKEN, 'new-token');
  //   });

  //   it('should not throw when sessionStorage is not available', () => {
  //     const nullStorageRef = { session: null };

  //     TestBed.resetTestingModule();
  //     TestBed.configureTestingModule({
  //       providers: [
  //         provideHttpClient(),
  //         provideHttpClientTesting(),
  //         AdminAuthService,
  //         { provide: SessionStorageRef, useValue: nullStorageRef }
  //       ]
  //     });
  //     const nullService = TestBed.inject(AdminAuthService);
  //     const nullHttpTestingController = TestBed.inject(HttpTestingController);

  //     expect(() => nullService.setToken('new-token')).not.toThrow();
  //     nullHttpTestingController.verify();
  //   });

  //   it('should remove token from sessionStorage', () => {
  //     service.clearToken();

  //     expect(sessionStorageMock.removeItem).toHaveBeenCalledWith(ADMIN_SESSION_TOKEN);
  //   });
  // });

  describe('isAuthenticated', () => {
    it('should return true when token exists', done => {
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
