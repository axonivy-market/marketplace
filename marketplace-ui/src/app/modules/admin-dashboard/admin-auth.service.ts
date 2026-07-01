import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { UserInfo } from '../../auth/auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import { ADMIN_SESSION_TOKEN } from '../../shared/constants/common.constant';

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private static readonly CSRF_COOKIE_NAME = 'XSRF-TOKEN';

  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly windowRef = inject(WindowRef);
  private readonly _userInfo = signal<UserInfo | null>(null);
  private readonly _csrfToken = signal<string | null>(null);
  readonly userInfo = this._userInfo.asReadonly();

  constructor() {
    const user = this.readStoredUser();
    if (user) {
      this._userInfo.set(user);
    }
  }

  loadFromSessionStorage(): UserInfo | null {
    return this.readStoredUser();
  }

  logout() {
    this.httpClient.post<void>(API_URI.ADMIN_LOGOUT, {}).pipe(
      catchError(() => of(void 0))
    ).subscribe({
      complete: () => {
        this.clearSessionState();
      }
    });
  }

  setUserInfo(userInfo: UserInfo): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, JSON.stringify(userInfo));
    this._userInfo.set(userInfo);
  }

  fetchCsrfToken(): Observable<void> {
    return this.httpClient.get<void>(API_URI.ADMIN_CSRF).pipe(
      tap(() => this._csrfToken.set(this.getLiveCsrfToken()))
    );
  }

  clearToken(): void {
    this.clearSessionState();
  }

  isAuthenticated(): Observable<boolean> {
    return this.httpClient.get<UserInfo>(API_URI.ADMIN_SESSION).pipe(
      tap(userInfo => this.setUserInfo(userInfo)),
      map(() => true),
      catchError(() => {
        this.clearSessionState();
        return of(false);
      })
    );
  }

  csrfToken(): string | null {
    return this.getLiveCsrfToken() ?? this._csrfToken();
  }

  private readStoredUser(): UserInfo | null {
    const storedUserInfo = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN);
    return storedUserInfo ? JSON.parse(storedUserInfo) : null;
  }

  private clearSessionState(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
    this._userInfo.set(null);
    this._csrfToken.set(null);
  }

  private getLiveCsrfToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const cookie = this.windowRef.nativeWindow?.document.cookie ?? '';
    return this.readCookie(cookie, AdminAuthService.CSRF_COOKIE_NAME);
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
