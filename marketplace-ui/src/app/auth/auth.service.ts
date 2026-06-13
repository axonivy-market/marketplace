import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, Observable, switchMap, throwError } from 'rxjs';
import { WindowRef } from '../core/services/browser/window-ref.service';
import { RuntimeConfigService } from '../core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../core/models/runtime-config';
import { API_URI } from '../shared/constants/api.constant';
import { AdminAuthService } from '../modules/admin-dashboard/admin-auth.service';
import {
  serializePublicKeyCredential,
  supportsPasskeys,
  toAuthenticationOptions,
  toRegistrationOptions
} from './webauthn.util';

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
  hasPasskey?: boolean;
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

  isPasskeySupported(): boolean {
    return supportsPasskeys(this.windowRef.nativeWindow);
  }

  async loginWithPasskey(username?: string | null): Promise<void> {
    this.ensurePasskeySupport();
    await firstValueFrom(this.adminAuthService.fetchCsrfToken());

    const options = await firstValueFrom(
      this.http.post<Record<string, unknown>>(API_URI.ADMIN_PASSKEY_AUTHENTICATE_OPTIONS, {
        username: username?.trim() || null
      })
    );

    const credential = await this.windowRef.nativeWindow?.navigator.credentials.get(
      toAuthenticationOptions(options)
    ) as PublicKeyCredential | null;

    if (!credential) {
      throw new Error('Passkey authentication was cancelled');
    }

    const userInfo = await firstValueFrom(
      this.http.post<UserInfo>(API_URI.ADMIN_PASSKEY_AUTHENTICATE_COMPLETE, {
        credential: serializePublicKeyCredential(credential)
      })
    );

    this.handleAuthenticatedUser(userInfo);
  }

  async registerPasskey(): Promise<void> {
    this.ensurePasskeySupport();
    await firstValueFrom(this.adminAuthService.fetchCsrfToken());

    const options = await firstValueFrom(
      this.http.post<Record<string, unknown>>(API_URI.ADMIN_PASSKEY_REGISTER_OPTIONS, {})
    );

    const credential = await this.windowRef.nativeWindow?.navigator.credentials.create(
      toRegistrationOptions(options)
    ) as PublicKeyCredential | null;

    if (!credential) {
      throw new Error('Passkey registration was cancelled');
    }

    const userInfo = await firstValueFrom(
      this.http.post<UserInfo>(API_URI.ADMIN_PASSKEY_REGISTER_COMPLETE, {
        credential: serializePublicKeyCredential(credential)
      })
    );

    this.adminAuthService.setUserInfo(userInfo);
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

  private ensurePasskeySupport(): void {
    if (!this.isPasskeySupported()) {
      throw new Error('Passkeys are not available in this browser');
    }
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
