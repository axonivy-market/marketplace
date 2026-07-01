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
    csrfToken: ReturnType<typeof vi.fn>;
    setUserInfo: ReturnType<typeof vi.fn>;
    userInfo: ReturnType<typeof signal<UserInfo | null>>;
    loadFromSessionStorage: ReturnType<typeof vi.fn>;
  };
  const location = { origin: 'http://localhost', href: '' };
  const credentials = {
    create: vi.fn(),
    get: vi.fn()
  };
  const originalPublicKeyCredential = globalThis.PublicKeyCredential;

  beforeEach(() => {
    router = {
      navigate: vi.fn()
    };
    adminAuthService = {
      fetchCsrfToken: vi.fn().mockReturnValue(of({})),
      csrfToken: vi.fn().mockReturnValue('csrf'),
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
          useValue: {
            nativeWindow: {
              location,
              isSecureContext: true,
              navigator: { credentials },
              PublicKeyCredential: function PublicKeyCredential() {}
            }
          }
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
    credentials.create.mockReset();
    credentials.get.mockReset();
    (globalThis as any).PublicKeyCredential = function PublicKeyCredential() {};
  });

  afterEach(() => {
    httpMock.verify();
    (globalThis as any).PublicKeyCredential = originalPublicKeyCredential;
  });

  async function flushMicrotasks(times = 3): Promise<void> {
    for (let i = 0; i < times; i++) {
      await Promise.resolve();
    }
  }

  it('redirects to GitHub using a server-issued state', async () => {
    service.redirectToGitHub('/ignored');
    await flushMicrotasks();

    const request = httpMock.expectOne(API_URI.ADMIN_GITHUB_AUTHORIZATION);
    expect(request.request.method).toBe('GET');
    request.flush({ state: 'server-state' });
    await flushMicrotasks();

    expect(adminAuthService.fetchCsrfToken).toHaveBeenCalled();
    expect(location.href).toContain('client_id=github-client-id');
    expect(location.href).toContain('redirect_uri=http://localhost/auth/github/callback');
    expect(location.href).toContain('state=server-state');
  });

  it('exchanges callback code for a session and navigates to the dashboard', async () => {
    const userInfo: UserInfo = {
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: 'https://avatar',
      url: 'https://github.com/octopus'
    };

    service.handleGitHubCallback('code-1', 'state-1');
    await flushMicrotasks();

    const request = httpMock.expectOne(API_URI.ADMIN_GITHUB_CALLBACK);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ code: 'code-1', state: 'state-1' });
    request.flush(userInfo);

    await Promise.resolve();
    await Promise.resolve();

    expect(adminAuthService.fetchCsrfToken).toHaveBeenCalled();
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

  it('registers a passkey and refreshes the local user session', async () => {
    const createdCredential = {
      id: 'credential-1',
      rawId: new Uint8Array([1, 2, 3]).buffer,
      type: 'public-key',
      authenticatorAttachment: 'platform',
      getClientExtensionResults: () => ({}),
      response: {
        clientDataJSON: new Uint8Array([1]).buffer,
        attestationObject: new Uint8Array([2]).buffer,
        getTransports: () => ['internal']
      }
    };
    credentials.create.mockResolvedValue(createdCredential);

    const registerPromise = service.registerPasskey();
    await flushMicrotasks();

    const optionsRequest = httpMock.expectOne(API_URI.ADMIN_PASSKEY_REGISTER_OPTIONS);
    expect(optionsRequest.request.method).toBe('POST');
    optionsRequest.flush({
      challenge: 'AQID',
      rp: { id: 'localhost', name: 'Axon Ivy Marketplace Admin' },
      user: { id: 'BAUG', name: 'octopus', displayName: 'Octopus' },
      pubKeyCredParams: [],
      timeout: 300000
    });
    await flushMicrotasks();

    const completeRequest = httpMock.expectOne(API_URI.ADMIN_PASSKEY_REGISTER_COMPLETE);
    expect(completeRequest.request.method).toBe('POST');
    completeRequest.flush({
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: '',
      url: 'https://github.com/octopus',
      hasPasskey: true
    });

    await registerPromise;

    expect(adminAuthService.setUserInfo).toHaveBeenCalledWith(expect.objectContaining({ hasPasskey: true }));
  });

  it('logs in with a passkey and navigates to the dashboard', async () => {
    const assertedCredential = {
      id: 'credential-1',
      rawId: new Uint8Array([1, 2, 3]).buffer,
      type: 'public-key',
      authenticatorAttachment: 'platform',
      getClientExtensionResults: () => ({}),
      response: {
        clientDataJSON: new Uint8Array([1]).buffer,
        authenticatorData: new Uint8Array([2]).buffer,
        signature: new Uint8Array([3]).buffer,
        userHandle: new Uint8Array([4]).buffer
      }
    };
    credentials.get.mockResolvedValue(assertedCredential);

    const loginPromise = service.loginWithPasskey('octopus');
    await flushMicrotasks();

    const optionsRequest = httpMock.expectOne(API_URI.ADMIN_PASSKEY_AUTHENTICATE_OPTIONS);
    expect(optionsRequest.request.method).toBe('POST');
    expect(optionsRequest.request.body).toEqual({ username: 'octopus' });
    optionsRequest.flush({
      challenge: 'AQID',
      rpId: 'localhost',
      timeout: 300000,
      allowCredentials: [{ id: 'BAUG', type: 'public-key' }]
    });
    await flushMicrotasks();

    const completeRequest = httpMock.expectOne(API_URI.ADMIN_PASSKEY_AUTHENTICATE_COMPLETE);
    expect(completeRequest.request.method).toBe('POST');
    completeRequest.flush({
      id: 'user-1',
      token: null,
      login: 'octopus',
      name: 'Octopus',
      avatarUrl: '',
      url: 'https://github.com/octopus',
      hasPasskey: true
    });

    await loginPromise;

    expect(adminAuthService.setUserInfo).toHaveBeenCalledWith(expect.objectContaining({ id: 'user-1' }));
    expect(router.navigate).toHaveBeenCalledWith(['/internal-dashboard']);
  });
});
