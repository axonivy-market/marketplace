import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { WindowRef } from '../core/services/browser/window-ref.service';
import { RuntimeConfigService } from '../core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../core/models/runtime-config';
import { API_URI } from '../shared/constants/api.constant';
import { AdminAuthService } from '../modules/admin-dashboard/admin-auth.service';

export interface TokenPayload {
  username: string;
  name: string;
  sub: string;
  exp: number;
  accessToken?: string;
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
  private readonly githubOAuthCallbackUrl: string;
  private readonly githubAdminOAuthCallbackUrl: string;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly windowRef: WindowRef,
    private readonly runtimeConfig: RuntimeConfigService,
    private readonly adminAuthService: AdminAuthService
  ) {
    const win = this.windowRef.nativeWindow;
    const callbackPath = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_CALLBACK);
    const adminCallbackPath = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_ADMIN_OAUTH_CALLBACK);
    this.githubOAuthCallbackUrl = `${win?.location?.origin ?? ''}${callbackPath}`;
    this.githubAdminOAuthCallbackUrl = `${win?.location?.origin ?? ''}${adminCallbackPath}`;
  }

  redirectToGitHub(originalUrl: string, options?: { useAdminState?: boolean }): void {
    void this.redirectToGitHubInternal(originalUrl, options?.useAdminState ?? false);
  }

  handleGitHubCallback(code: string, state: string): void {
    void this.handleGitHubCallbackInternal(code, state, API_URI.ADMIN_GITHUB_CALLBACK);
  }

  handleGitHubAdminCallback(code: string, state: string): void {
    void this.handleGitHubCallbackInternal(code, state, this.githubAdminOAuthCallbackUrl);
  }

  private async redirectToGitHubInternal(originalUrl: string, useAdminState: boolean): Promise<void> {
    const state = useAdminState ? await this.fetchGitHubAdminAuthorizationState() : originalUrl;
    this.redirectWindowToGitHub(this.buildGitHubAuthorizationUrl(state));
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

  private buildGitHubAuthorizationUrl(state: string): string {
    const githubClientId = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_APP_CLIENT_ID);
    return `${this.githubAuthUrl}?client_id=${githubClientId}&redirect_uri=${this.githubOAuthCallbackUrl}&state=${encodeURIComponent(state)}`;
  }

  private async fetchGitHubAdminAuthorizationState(): Promise<string> {
    await this.ensureCsrfToken();
    const { state } = await firstValueFrom(this.http.get<GitHubAuthorizationState>(this.githubAdminOAuthCallbackUrl));
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

  getToken(): string | null {
    return this.currentUser()?.token ?? null;
  }

  getDisplayName(): string | null {
    const userInfo = this.currentUser();
    return userInfo?.name || userInfo?.username || null;
  }

  getUserId(): string | null {
    const userInfo = this.currentUser();
    return userInfo?.id ?? null;
  }

  decodeToken(token: string): TokenPayload | null {
    try {
      return jwtDecode(token);
    } catch (error) {
      return null;
    }
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
}
