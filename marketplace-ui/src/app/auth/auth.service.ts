import { HttpBackend, HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, throwError } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { jwtDecode } from 'jwt-decode';
import { FEEDBACK_APPROVAL_STATE, TOKEN_KEY } from '../shared/constants/common.constant';
import { WindowRef } from '../core/services/browser/window-ref.service';
import { RuntimeConfigService } from '../core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../core/models/runtime-config';

export interface TokenPayload {
  username: string;
  name: string;
  sub: string;
  exp: number;
}

export interface RequestBody {
  [key: string]: string;
}

export interface TokenResponse {
  token: string;
}

export interface GitHubUser {
  login: string;
  name: string | null;
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

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly cookieService: CookieService,
    private readonly httpBackend: HttpBackend,
    private readonly windowRef: WindowRef,
    private readonly runtimeConfig: RuntimeConfigService
  ) {
    this.httpClientWithoutInterceptor = new HttpClient(httpBackend);

    this.BASE_URL = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.API_URL);
    this.userApiUrl = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.GITHUB_API_URL) + '/user';

    const win = this.windowRef.nativeWindow;
    const callbackPath = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.GITHUB_OAUTH_CALLBACK);
    this.githubOAuthCallbackUrl = (win?.location?.origin ?? '') + callbackPath;
  }

  redirectToGitHub(originalUrl: string): void {
    const state = encodeURIComponent(originalUrl);
    const githubClientId = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.GITHUB_OAUTH_APP_CLIENT_ID);
    const authUrl = `${this.githubAuthUrl}?client_id=${githubClientId}&redirect_uri=${this.githubOAuthCallbackUrl}&state=${state}`;

    const win = this.windowRef.nativeWindow;
    if (win) {
      win.location.href = authUrl;
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
    return this.http
      .post<TokenResponse>(url, body)
      .pipe(catchError(error => throwError(() => error)));
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

  getToken(): string | null {
    const token = this.cookieService.get(TOKEN_KEY);
    if (token && !this.isTokenExpired(token)) {
      return token;
    }
    return null;
  }

  decodeToken(token: string): TokenPayload | null {
    try {
      return jwtDecode(token);
    } catch (error) {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded = this.decodeToken(token);
      if (decoded) {
        if (!decoded?.exp) {
          return false;
        }
        const currentTime = Math.floor(Date.now() / 1000);
        return decoded.exp < currentTime;
      }
    } catch (error) {
      return true;
    }
    return false;
  }

  getDisplayName(): string | null {
    const token = this.getToken();
    if (token && this.decodeToken(token)) {
      const decoded = this.decodeToken(token);
      if (decoded) {
        return decoded.name || decoded.username;
      }
      return decoded;
    }
    return null;
  }

  getUserId(): string | null {
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

  private extractNumberOfExpiredDay(token: string): number {
    const exp = this.decodeToken(token)?.exp ?? 0;

    const expDate = new Date(exp * 1000);
    const currentDate = new Date();

    const diffTime = Math.abs(expDate.getTime() - currentDate.getTime());
    const dayInMilliseconds = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.DAY_IN_MILLISECONDS);
    return Math.ceil(diffTime / dayInMilliseconds);
  }

  getUserInfo(token: string): Observable<GitHubUser> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/vnd.github+json'
    });
    return this.httpClientWithoutInterceptor.get<GitHubUser>(this.userApiUrl, { headers }).pipe(
      map(response => ({
        login: response.login,
        name: response.name
      })),
      catchError(() => of({ login: '', name: null }))
    );
  }

  getDisplayNameFromAccessToken(token: string): Observable<string | null> {
    return this.getUserInfo(token).pipe(
      map(userInfo =>
        userInfo.name ?? userInfo.login ?? null)
    );
  }
}