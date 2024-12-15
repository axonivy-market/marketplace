import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { TOKEN_KEY } from '../shared/constants/common.constant';

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
});
