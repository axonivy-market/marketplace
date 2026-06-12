import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService, UserInfo } from './auth.service';
import { AdminAuthService } from '../modules/admin-dashboard/admin-auth.service';
import { WindowRef } from '../core/services/browser/window-ref.service';
import { RuntimeConfigService } from '../core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../core/models/runtime-config';
import { API_URI } from '../shared/constants/api.constant';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: { navigate: ReturnType<typeof vi.fn> };
  let adminAuthService: {
    fetchCsrfToken: ReturnType<typeof vi.fn>;
    setUserInfo: ReturnType<typeof vi.fn>;
    userInfo: ReturnType<typeof signal<UserInfo | null>>;
    loadFromSessionStorage: ReturnType<typeof vi.fn>;
  };
  const location = { origin: 'http://localhost', href: '' };

  beforeEach(() => {
    router = {
      navigate: vi.fn()
    };
    adminAuthService = {
      fetchCsrfToken: vi.fn().mockReturnValue(of({})),
      setUserInfo: vi.fn(),
      userInfo: signal<UserInfo | null>(null),
      loadFromSessionStorage: vi.fn().mockReturnValue(null)
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: router
        },
        {
          provide: AdminAuthService,
          useValue: adminAuthService
        },
        {
          provide: WindowRef,
          useValue: { nativeWindow: { location } }
        },
        {
          provide: RuntimeConfigService,
          useValue: {
            get: (key: string) => {
              switch (key) {
                case RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_APP_CLIENT_ID:
                  return 'github-client-id';
                case RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_CALLBACK:
                  return '/auth/github/callback';
                default:
                  return '';
              }
            }
          }
        }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    location.href = '';
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('redirects to GitHub using a server-issued state', () => {
    service.redirectToGitHub('/ignored');

    const request = httpMock.expectOne(API_URI.ADMIN_GITHUB_AUTHORIZATION);
    expect(request.request.method).toBe('GET');
    request.flush({ state: 'server-state' });

    expect(adminAuthService.fetchCsrfToken).toHaveBeenCalled();
    expect(location.href).toContain('client_id=github-client-id');
    expect(location.href).toContain('redirect_uri=http://localhost/auth/github/callback');
    expect(location.href).toContain('state=server-state');
  });

  it('exchanges callback code for a session and navigates to the dashboard', () => {
    const userInfo: UserInfo = {
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: 'https://avatar',
      url: 'https://github.com/octopus'
    };

    service.handleGitHubCallback('code-1', 'state-1');

    const request = httpMock.expectOne(API_URI.ADMIN_GITHUB_CALLBACK);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ code: 'code-1', state: 'state-1' });
    request.flush(userInfo);

    expect(adminAuthService.setUserInfo).toHaveBeenCalledWith(userInfo);
    expect(router.navigate).toHaveBeenCalledWith(['/internal-dashboard']);
  });

  it('reads display name and user id from the stored session user', () => {
    adminAuthService.loadFromSessionStorage.mockReturnValue({
      id: 'user-2',
      token: null,
      login: 'market-user',
      name: 'Market User',
      avatarUrl: '',
      url: ''
    });

    expect(service.getDisplayName()).toBe('Market User');
    expect(service.getUserId()).toBe('user-2');
    expect(service.getToken()).toBeNull();
  });
});
