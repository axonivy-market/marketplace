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
    const initPromise = service.initializeSecurity();

    const request = httpMock.expectOne(API_URI.ADMIN_CSRF);
    expect(request.request.method).toBe('GET');
    request.flush({ token: 'csrf' });

    await initPromise;
    expect(service.csrfToken()).toBe('csrf');
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
});
