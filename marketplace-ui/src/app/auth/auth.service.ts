import { HttpBackend, HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, Observable, throwError } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { WindowRef } from '../core/services/browser/window-ref.service';
import { RuntimeConfigService } from '../core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../core/models/runtime-config';
import { API_URI } from '../shared/constants/api.constant';
import { AdminAuthService } from '../modules/admin-dashboard/admin-auth.service';
import { FEEDBACK_APPROVAL_STATE, TOKEN_KEY } from '../shared/constants/common.constant';
import { CookieService } from 'ngx-cookie-service';

export interface TokenPayload {
  username: string;
  name: string;
  sub: string;
  exp: number;
  accessToken?: string;
}

export interface RequestBody {
  [key: string]: string;
}

export interface TokenResponse {
  token: string;
}
export interface GitHubAuthorizationState {
  state: string;
}

export interface GitHubUser {
  login: string;
  name: string | null;
  avatarUrl: string;
  url: string;
  username?: string;
}

export interface UserInfo extends GitHubUser {
  id?: string;
  gitHubId?: string;
  provider?: string;
  token: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly githubAuthUrl = 'https://github.com/login/oauth/authorize';
  private readonly httpClientWithoutInterceptor: HttpClient;
  private readonly BASE_URL: string;
  private readonly userApiUrl: string;
  private readonly githubOAuthCallbackUrl: string;
  private readonly githubAdminOAuthCallbackUrl: string;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly windowRef: WindowRef,
    private readonly cookieService: CookieService,
    private readonly httpBackend: HttpBackend,
    private readonly runtimeConfig: RuntimeConfigService,
    private readonly adminAuthService: AdminAuthService
  ) {
    this.httpClientWithoutInterceptor = new HttpClient(httpBackend);
    this.BASE_URL = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_API_URL);
    this.userApiUrl = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_API_URL) + '/user';
    const win = this.windowRef.nativeWindow;
    const callbackPath = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_CALLBACK);
    const adminCallbackPath = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_ADMIN_OAUTH_CALLBACK);
    this.githubOAuthCallbackUrl = `${win?.location?.origin ?? ''}${callbackPath}`;
    this.githubAdminOAuthCallbackUrl = `${win?.location?.origin ?? ''}${adminCallbackPath}`;
  }

  async redirectToGitHub(originalUrl: string, options?: { useAdminState?: boolean }): Promise<void> {
    const state = options?.useAdminState ? await this.fetchGitHubAdminAuthorizationState() : originalUrl;
    const redirectUrl = options?.useAdminState ? this.githubAdminOAuthCallbackUrl : this.githubOAuthCallbackUrl;
    this.redirectWindowToGitHub(this.buildGitHubAuthorizationUrl(state, redirectUrl));
  }

  getToken(): string | null {
    return this.currentUser()?.token ?? null;
  }

  decodeToken(token: string): TokenPayload | null {
    try {
      return jwtDecode(token);
    } catch (error) {
      return null;
    }
  }

  handleGitHubCallback(code: string, state: string): void {
    const body = { code };

    this.exchangeCodeForToken(body).subscribe({
      next: response => this.handleTokenResponse(response.token, state),
      error: error => throwError(() => error)
    });
  }

  private exchangeCodeForToken(body: RequestBody): Observable<TokenResponse> {
    const url = `${this.BASE_URL}/auth/github/login`;
    return this.http.post<TokenResponse>(url, body).pipe(catchError(error => throwError(() => error)));
  }

  handleTokenResponse(token: string, state: string): void {
    this.setTokenAsCookie(token);
    if (FEEDBACK_APPROVAL_STATE === state) {
      this.router.navigate([`${state}`]);
    } else {
      this.router.navigate([`${state}`], {
        queryParams: { showPopup: 'true' }
      });
    }
  }

  private setTokenAsCookie(token: string): void {
    this.cookieService.set(TOKEN_KEY, token, {
      expires: this.extractNumberOfExpiredDay(token),
      path: '/'
    });
  }

  handleGitHubAdminCallback(code: string, state: string): void {
    void this.handleGitHubCallbackInternal(code, state, API_URI.ADMIN_GITHUB_CALLBACK);
  }

  private async handleGitHubCallbackInternal(code: string, state: string, callBackUrl: string): Promise<void> {
    const userInfo = await firstValueFrom(this.http.post<UserInfo>(callBackUrl, { code, state }));
    await this.handleAuthenticatedUser(userInfo);
  }

  private async handleAuthenticatedUser(userInfo: UserInfo): Promise<void> {
    await this.ensureCsrfToken();
    this.adminAuthService.setUserInfo(userInfo);
    this.router.navigate(['/internal-dashboard']);
  }

  private buildGitHubAuthorizationUrl(state: string, redirectUri: string): string {
    const githubClientId = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_APP_CLIENT_ID);
    return `${this.githubAuthUrl}?client_id=${githubClientId}&redirect_uri=${redirectUri}&state=${encodeURIComponent(state)}`;
  }

  private async fetchGitHubAdminAuthorizationState(): Promise<string> {
    await this.ensureCsrfToken();
    const { state } = await firstValueFrom(this.http.get<GitHubAuthorizationState>(API_URI.ADMIN_GITHUB_AUTHORIZATION));
    return state;
  }

  private redirectWindowToGitHub(authUrl: string): void {
    const win = this.windowRef.nativeWindow;
    if (win) {
      win.location.href = authUrl;
    }
  }

  private async ensureCsrfToken(): Promise<void> {
    await firstValueFrom(this.adminAuthService.fetchCsrfToken());
  }

  getDisplayName(): string | null {
    const userInfo = this.currentUser();
    return userInfo?.name || userInfo?.username || null;
  }

  getUserId(): string | null {
    const userInfo = this.currentUser();
    return userInfo?.id ?? null;
  }

  getFeedbackUserId(): string | null {
    const token = this.getToken();
    if (token) {
      const decoded = this.decodeToken(token);
      if (decoded) {
        return decoded.sub;
      }
      return null;
    }
    return null;
  }

  private currentUser(): UserInfo | null {
    return this.adminAuthService.userInfo() ?? this.adminAuthService.loadFromSessionStorage();
  }

  private extractNumberOfExpiredDay(token: string): number {
    const exp = this.decodeToken(token)?.exp ?? 0;

    const expDate = new Date(exp * 1000);
    const currentDate = new Date();

    const diffTime = Math.abs(expDate.getTime() - currentDate.getTime());
    const dayInMilliseconds = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_DAY_IN_MILLISECONDS);
    return Math.ceil(diffTime / dayInMilliseconds);
  }
}
