import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, catchError, firstValueFrom, of, tap } from 'rxjs';
import { UserInfo } from '../../auth/auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import { ADMIN_SESSION_TOKEN } from '../../shared/constants/common.constant';

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly _userInfo = signal<UserInfo | null>(null);
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
        this._userInfo.set(null);
      }
    });
  }

  setUserInfo(userInfo: UserInfo): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, JSON.stringify(userInfo));
    this._userInfo.set(userInfo);
  }

  fetchCsrfToken(): Observable<unknown> {
    return this.httpClient.get(API_URI.ADMIN_CSRF);
  }

  clearToken(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
    this._userInfo.set(null);
  }

  isAuthenticated(): Observable<boolean> {
    return of(this.userInfo() !== null);
  }
}
