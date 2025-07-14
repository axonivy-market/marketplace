import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from './auth.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { TOKEN_KEY } from '../shared/constants/common.constant';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;
  let cookieServiceSpy: jasmine.SpyObj<CookieService>;

  beforeEach(() => {
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);
    const cookieSpyObj = jasmine.createSpyObj('CookieService', ['set', 'get']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Router, useValue: routerSpyObj },
        { provide: CookieService, useValue: cookieSpyObj }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    cookieServiceSpy = TestBed.inject(CookieService) as jasmine.SpyObj<CookieService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should exchange code for token', () => {
    const code = 'testCode';
    const state = 'testState';
    const mockResponse = { token: 'testToken' };

    spyOn(service, 'handleTokenResponse').and.callThrough();

    service.handleGitHubCallback(code, state);

    const req = httpMock.expectOne(`${service['BASE_URL']}/auth/github/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);

    expect(service['handleTokenResponse']).toHaveBeenCalledWith(mockResponse.token, state);
  });

  it('should handle error during token exchange', () => {
    const code = 'testCode';
    const state = 'testState';
    service.handleGitHubCallback(code, state);

    const req = httpMock.expectOne(`${service['BASE_URL']}/auth/github/login`);
    expect(req.request.method).toBe('POST');
    req.error(new ErrorEvent('Network error'));
  });

  it('should set token as cookie and navigate', () => {
    const token = 'testToken';
    const state = 'testState';

    service['handleTokenResponse'](token, state);

    expect(cookieServiceSpy.set).toHaveBeenCalledWith(
      TOKEN_KEY,
      token,
      {expires: jasmine.any(Number), path: '/'}
    );
    expect(routerSpy.navigate).toHaveBeenCalledWith([state], {
      queryParams: { showPopup: 'true' }
    });
  });

  it('should return null if token is expired', () => {
    const token = 'expiredToken';
    spyOn(service as any, 'isTokenExpired').and.returnValue(true);
    cookieServiceSpy.get.and.returnValue(token);

    const result = service.getToken();

    expect(result).toBeNull();
  });

  it('should return token if not expired', () => {
    const token = 'validToken';
    spyOn(service as any, 'isTokenExpired').and.returnValue(false);
    cookieServiceSpy.get.and.returnValue(token);

    const result = service.getToken();

    expect(result).toBe(token);
  });

  it('should return display name from decoded token', () => {
    const token = 'validToken';
    const decodedToken = { name: 'testName' };
    spyOn(service as any, 'decodeToken').and.returnValue(decodedToken);
    spyOn(service, 'getToken').and.returnValue(token);

    const result = service.getDisplayName();

    expect(result).toBe(decodedToken.name);
  });

  it('should return user ID from decoded token', () => {
    const token = 'validToken';
    const decodedToken = { sub: 'testUserId' };
    spyOn(service as any, 'decodeToken').and.returnValue(decodedToken);
    spyOn(service, 'getToken').and.returnValue(token);

    const result = service.getUserId();

    expect(result).toBe(decodedToken.sub);
  });

  it('should extract number of expired days correctly', () => {
    const token = 'validToken';
    const decodedToken = { exp: Math.floor(Date.now() / 1000) + 86400 };
    spyOn(service as any, 'decodeToken').and.returnValue(decodedToken);

    const result = service['extractNumberOfExpiredDay'](token);

    expect(result).toBe(1);
  });

  it('should fetch user info successfully', () => {
    const token = 'mockToken';
    const mockUserResponse = {
      login: 'mockuser',
      name: 'Mock User'
    };

    service.getUserInfo(token).subscribe(user => {
      expect(user).toEqual(mockUserResponse);
    });

    const req = httpMock.expectOne(`${environment.githubApiUrl}/user`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    expect(req.request.headers.get('Accept')).toBe('application/vnd.github+json');

    req.flush(mockUserResponse);
  });

  it('should return fallback user when request fails', () => {
    const token = 'mockToken';

    service.getUserInfo(token).subscribe(user => {
      expect(user).toEqual({ login: '', name: null });
    });

    const req = httpMock.expectOne(`${environment.githubApiUrl}/user`);
    expect(req.request.method).toBe('GET');

    req.error(new ErrorEvent('Network error'));
  });

  it('should return user\'s name if available', () => {
    const token = 'mockToken';
    const mockUser = { login: 'mockuser', name: 'Mock User' };

    service.getDisplayNameFromAccessToken(token).subscribe(name => {
      expect(name).toBe('Mock User');
    });

    const req = httpMock.expectOne(`${environment.githubApiUrl}/user`);
    req.flush(mockUser);
  });

  it('should return login if name is null', () => {
    const token = 'mockToken';
    const mockUser = { login: 'mockuser', name: null };

    service.getDisplayNameFromAccessToken(token).subscribe(name => {
      expect(name).toBe('mockuser');
    });

    const req = httpMock.expectOne(`${environment.githubApiUrl}/user`);
    req.flush(mockUser);
  });

  it('should return null if both name and login are missing', () => {
    const token = 'mockToken';
    const mockUser = { login: null, name: null };

    service.getDisplayNameFromAccessToken(token).subscribe(name => {
      expect(name).toBeNull();
    });

    const req = httpMock.expectOne(`${environment.githubApiUrl}/user`);
    req.flush(mockUser);
  });

  it('should return true if token is expired', () => {
    const token = 'expiredToken';
    const decoded = {
      exp: Math.floor(Date.now() / 1000) - 60 // expired 1 minute ago
    };

    spyOn(service as any, 'decodeToken').and.returnValue(decoded);

    const result = (service as any)['isTokenExpired'](token);
    expect(result).toBeTrue();
  });

  it('should return false if token is not expired', () => {
    const token = 'validToken';
    const decoded = {
      exp: Math.floor(Date.now() / 1000) + 60 // expires in 1 minute
    };

    spyOn(service as any, 'decodeToken').and.returnValue(decoded);

    const result = (service as any)['isTokenExpired'](token);
    expect(result).toBeFalse();
  });

  it('should return false if decoded token has no exp', () => {
    const token = 'noExpToken';
    const decoded = {}; // no exp field

    spyOn(service as any, 'decodeToken').and.returnValue(decoded);

    const result = (service as any)['isTokenExpired'](token);
    expect(result).toBeFalse();
  });

  it('should return true if decoding fails', () => {
    const token = 'badToken';

    spyOn(service as any, 'decodeToken').and.throwError('Invalid token');

    const result = (service as any)['isTokenExpired'](token);
    expect(result).toBeTrue();
  });
});
