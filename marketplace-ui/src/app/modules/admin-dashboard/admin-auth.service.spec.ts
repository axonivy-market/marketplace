import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { AdminAuthService } from './admin-auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import { UserInfo } from '../../auth/auth.service';
import { firstValueFrom } from 'rxjs';

describe('AdminAuthService', () => {
  let service: AdminAuthService;
  let httpMock: HttpTestingController;
  const session = {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn()
  };

  beforeEach(() => {
    session.getItem.mockReset();
    session.setItem.mockReset();
    session.removeItem.mockReset();
    session.getItem.mockReturnValue(null);
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';

    TestBed.configureTestingModule({
      providers: [
        AdminAuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: SessionStorageRef,
          useValue: { session }
        },
        {
          provide: PLATFORM_ID,
          useValue: 'browser'
        }
      ]
    });

    service = TestBed.inject(AdminAuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches a csrf token during initialization', async () => {
    document.cookie = 'XSRF-TOKEN=csrf; path=/';
    const initPromise = service.initializeSecurity();

    const request = httpMock.expectOne(API_URI.ADMIN_CSRF);
    expect(request.request.method).toBe('GET');
    request.flush({ token: 'ignored-body-token' });

    await initPromise;
    expect(service.csrfToken()).toBe('csrf');
  });

  it('prefers the live csrf cookie over the cached token', async () => {
    const initPromise = service.initializeSecurity();

    const request = httpMock.expectOne(API_URI.ADMIN_CSRF);
    request.flush({ token: 'cached-csrf' });

    await initPromise;
    document.cookie = 'XSRF-TOKEN=cookie-csrf; path=/';

    expect(service.csrfToken()).toBe('cookie-csrf');
  });

  it('falls back to the csrf token from the response body when the cookie is unavailable', async () => {
    const initPromise = service.initializeSecurity();

    const request = httpMock.expectOne(API_URI.ADMIN_CSRF);
    request.flush({ token: 'response-csrf' });

    await initPromise;

    expect(service.csrfToken()).toBe('response-csrf');
  });

  it('stores and clears the session user locally', () => {
    const userInfo: UserInfo = {
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: '',
      url: ''
    };

    service.setUserInfo(userInfo);
    expect(session.setItem).toHaveBeenCalled();
    expect(service.userInfo()).toEqual(userInfo);

    service.clearToken();
    expect(session.removeItem).toHaveBeenCalled();
    expect(service.userInfo()).toBeNull();
  });

  it('posts to logout and clears the local user state', () => {
    service.setUserInfo({
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: '',
      url: ''
    });

    service.logout();

    const request = httpMock.expectOne(API_URI.ADMIN_LOGOUT);
    expect(request.request.method).toBe('POST');
    request.flush({});

    expect(service.userInfo()).toBeNull();
  });

  it('validates the session against the backend before allowing access', async () => {
    session.getItem.mockReturnValue(
      JSON.stringify({
        id: 'fake-user',
        token: null,
        login: 'fake',
        name: 'Fake',
        avatarUrl: '',
        url: ''
      })
    );

    const resultPromise = firstValueFrom(service.isAuthenticated());

    const request = httpMock.expectOne(API_URI.ADMIN_SESSION);
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: '',
      url: '',
      hasPasskey: true
    });

    await expect(resultPromise).resolves.toBe(true);
    expect(service.userInfo()).toEqual(
      expect.objectContaining({
        id: 'user-1',
        login: 'octopus'
      })
    );
    expect(session.setItem).toHaveBeenCalledWith(
      'admin-session-token',
      expect.stringContaining('"user-1"')
    );
  });

  it('clears local state when the backend session probe fails', async () => {
    service.setUserInfo({
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: '',
      url: ''
    });

    const resultPromise = firstValueFrom(service.isAuthenticated());

    const request = httpMock.expectOne(API_URI.ADMIN_SESSION);
    expect(request.request.method).toBe('GET');
    request.flush({ message: 'unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    await expect(resultPromise).resolves.toBe(false);
    expect(service.userInfo()).toBeNull();
    expect(session.removeItem).toHaveBeenCalledWith('admin-session-token');
  });
});
