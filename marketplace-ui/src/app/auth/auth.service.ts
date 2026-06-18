import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
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
    this.githubOAuthCallbackUrl = `${win?.location?.origin ?? ''}${callbackPath}`;
  }

  redirectToGitHub(_originalUrl: string): void {
    void this.redirectToGitHubInternal();
  }

  handleGitHubCallback(code: string, state: string): void {
    void this.handleGitHubCallbackInternal(code, state);
  }

  isPasskeySupported(): boolean {
    return supportsPasskeys(this.windowRef.nativeWindow);
  }

  async loginWithPasskey(username?: string | null): Promise<void> {
    this.ensurePasskeySupport();
    await this.completePasskeyFlow(
      API_URI.ADMIN_PASSKEY_AUTHENTICATE_OPTIONS,
      { username: username?.trim() || null },
      API_URI.ADMIN_PASSKEY_AUTHENTICATE_COMPLETE,
      async options =>
        this.windowRef.nativeWindow?.navigator.credentials.get(
          toAuthenticationOptions(options)
        ) as Promise<PublicKeyCredential | null>,
      'Passkey authentication was cancelled'
    );
  }

  async registerPasskey(): Promise<void> {
    this.ensurePasskeySupport();
    await this.completePasskeyFlow(
      API_URI.ADMIN_PASSKEY_REGISTER_OPTIONS,
      {},
      API_URI.ADMIN_PASSKEY_REGISTER_COMPLETE,
      async options =>
        this.windowRef.nativeWindow?.navigator.credentials.create(
          toRegistrationOptions(options)
        ) as Promise<PublicKeyCredential | null>,
      'Passkey registration was cancelled'
    );
  }

  private async redirectToGitHubInternal(): Promise<void> {
    await this.ensureCsrfToken();
    const { state } = await firstValueFrom(
      this.http.get<GitHubAuthorizationState>(API_URI.ADMIN_GITHUB_AUTHORIZATION)
    );
    const win = this.windowRef.nativeWindow;
    if (win) {
      win.location.href = this.buildGitHubAuthorizationUrl(state);
    }
  }

  private async handleGitHubCallbackInternal(code: string, state: string): Promise<void> {
    const userInfo = await firstValueFrom(
      this.http.post<UserInfo>(API_URI.ADMIN_GITHUB_CALLBACK, { code, state })
    );
    await this.handleAuthenticatedUser(userInfo);
  }

  private async handleAuthenticatedUser(userInfo: UserInfo): Promise<void> {
    await this.ensureCsrfToken();
    this.adminAuthService.setUserInfo(userInfo);
    this.router.navigate(['/internal-dashboard']);
  }

  private async completePasskeyFlow(
    optionsUrl: string,
    optionsBody: Record<string, unknown>,
    completionUrl: string,
    credentialFactory: (options: Record<string, unknown>) => Promise<PublicKeyCredential | null>,
    cancellationMessage: string
  ): Promise<void> {
    await this.ensureCsrfToken();
    const options = await firstValueFrom(
      this.http.post<Record<string, unknown>>(optionsUrl, optionsBody)
    );
    const credential = await credentialFactory(options);
    if (!credential) {
      throw new Error(cancellationMessage);
    }

    const userInfo = await firstValueFrom(
      this.http.post<UserInfo>(completionUrl, {
        credential: serializePublicKeyCredential(credential)
      })
    );
    await this.handleAuthenticatedUser(userInfo);
  }

  private buildGitHubAuthorizationUrl(state: string): string {
    const githubClientId = this.runtimeConfig.get(
      RUNTIME_CONFIG_KEYS.MARKET_GITHUB_OAUTH_APP_CLIENT_ID
    );
    return `${this.githubAuthUrl}?client_id=${githubClientId}&redirect_uri=${this.githubOAuthCallbackUrl}&state=${encodeURIComponent(state)}`;
  }

  private async ensureCsrfToken(): Promise<void> {
    await firstValueFrom(this.adminAuthService.fetchCsrfToken());
  }

  private ensurePasskeySupport(): void {
    if (!this.isPasskeySupported()) {
      throw new Error('Passkeys are not available in this browser');
    }
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

  private currentUser(): UserInfo | null {
    return this.adminAuthService.userInfo() ?? this.adminAuthService.loadFromSessionStorage();
  }
}
