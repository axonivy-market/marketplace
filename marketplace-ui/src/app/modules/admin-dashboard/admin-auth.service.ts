import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, catchError, firstValueFrom, map, of, tap } from 'rxjs';
import { UserInfo } from '../../auth/auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import { ADMIN_SESSION_TOKEN } from '../../shared/constants/common.constant';

interface CsrfTokenResponse {
  token: string;
  headerName?: string;
  parameterName?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly windowRef = inject(WindowRef);
  private readonly _userInfo = signal<UserInfo | null>(null);
  private readonly _csrfToken = signal<string | null>(null);
  readonly userInfo = this._userInfo.asReadonly();

  constructor() {
    const user = this.loadFromSessionStorage();
    if (user) {
      this._userInfo.set(user);
    }
  }

  initializeSecurity(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return Promise.resolve();
    }

    return firstValueFrom(this.fetchCsrfToken().pipe(catchError(() => of(null)))).then(() => undefined);
  }

  loadFromSessionStorage(): UserInfo | null {
    const storedUserInfo = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN);
    return storedUserInfo ? JSON.parse(storedUserInfo) : null;
  }

  logout() {
    this.httpClient.post<void>(API_URI.ADMIN_LOGOUT, {}).pipe(
      catchError(() => of(void 0))
    ).subscribe({
      complete: () => {
        this.clearToken();
      }
    });
  }

  setUserInfo(userInfo: UserInfo): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, JSON.stringify(userInfo));
    this._userInfo.set(userInfo);
  }

  fetchCsrfToken(): Observable<CsrfTokenResponse> {
    return this.httpClient.get<CsrfTokenResponse>(API_URI.ADMIN_CSRF).pipe(
      tap(response => this._csrfToken.set(this.getLiveCsrfToken() ?? response?.token ?? null))
    );
  }

  clearToken(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
    this._userInfo.set(null);
    this._csrfToken.set(null);
  }

  isAuthenticated(): Observable<boolean> {
    return this.httpClient.get<UserInfo>(API_URI.ADMIN_SESSION).pipe(
      tap(userInfo => this.setUserInfo(userInfo)),
      map(() => true),
      catchError(() => {
        this.clearToken();
        return of(false);
      })
    );
  }

  csrfToken(): string | null {
    return this.getLiveCsrfToken() ?? this._csrfToken();
  }

  private currentUser(): UserInfo | null {
    return this.userInfo() ?? this.loadFromSessionStorage();
  }

  private getLiveCsrfToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const cookie = this.windowRef.nativeWindow?.document.cookie ?? '';
    return this.readCookie(cookie, 'XSRF-TOKEN');
  }

  private readCookie(cookieHeader: string, cookieName: string): string | null {
    const parts = cookieHeader.split(';');
    for (let i = parts.length - 1; i >= 0; i--) {
      const [rawName, ...rawValueParts] = parts[i].trim().split('=');
      if (rawName === cookieName) {
        const rawValue = rawValueParts.join('=');
        return rawValue ? decodeURIComponent(rawValue) : null;
      }
    }

    return null;
  }
}
