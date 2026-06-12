import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, of, switchMap, throwError } from 'rxjs';
import { WindowRef } from '../core/services/browser/window-ref.service';
import { RuntimeConfigService } from '../core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../core/models/runtime-config';
import { API_URI } from '../shared/constants/api.constant';
import { AdminAuthService } from '../modules/admin-dashboard/admin-auth.service';

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

  private readonly githubOAuthCallbackUrl: string;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly windowRef: WindowRef,
    private readonly runtimeConfig: RuntimeConfigService,
    private readonly adminAuthService: AdminAuthService
  ) {
    const win = this.windowRef.nativeWindow;
    const callbackPath = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_CALLBACK);
    this.githubOAuthCallbackUrl = (win?.location?.origin ?? '') + callbackPath;
  }

  redirectToGitHub(_originalUrl: string): void {
    const githubClientId = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_APP_CLIENT_ID);
    this.adminAuthService.fetchCsrfToken().pipe(
      switchMap(() => this.http.get<GitHubAuthorizationState>(API_URI.ADMIN_GITHUB_AUTHORIZATION))
    ).subscribe({
      next: ({ state }) => {
        const authUrl = `${this.githubAuthUrl}?client_id=${githubClientId}&redirect_uri=${this.githubOAuthCallbackUrl}&state=${encodeURIComponent(state)}`;
        const win = this.windowRef.nativeWindow;
        if (win) {
          win.location.href = authUrl;
        }
      }
    });
  }

  handleGitHubCallback(code: string, state: string): void {
    const body = { code, state };

    this.exchangeCodeForSession(body).subscribe({
      next: userInfo => this.handleAuthenticatedUser(userInfo),
      error: error => throwError(() => error)
    });
  }

  private exchangeCodeForSession(body: { code: string; state: string }): Observable<UserInfo> {
    return this.http
      .post<UserInfo>(API_URI.ADMIN_GITHUB_CALLBACK, body)
      .pipe(catchError(error => throwError(() => error)));
  }

  private handleAuthenticatedUser(userInfo: UserInfo): void {
    this.adminAuthService.setUserInfo(userInfo);
    this.router.navigate(['/internal-dashboard']);
  }

  getToken(): string | null {
    return this.adminAuthService.userInfo()?.id ?? null;
  }

  getDisplayName(): string | null {
    const userInfo = this.adminAuthService.userInfo() ?? this.adminAuthService.loadFromSessionStorage();
    return userInfo?.name || userInfo?.username || null;
  }

  getUserId(): string | null {
    const userInfo = this.adminAuthService.userInfo() ?? this.adminAuthService.loadFromSessionStorage();
    return userInfo?.id ?? null;
  }
}
