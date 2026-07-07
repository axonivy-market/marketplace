import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { UserInfo } from '../../auth/auth.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import { ADMIN_SESSION_TOKEN } from '../../shared/constants/common.constant';

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);
  private readonly _userInfo = signal<UserInfo | null>(null);
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
    return this.httpClient.get<void>(API_URI.ADMIN_CSRF);
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

  private readStoredUser(): UserInfo | null {
    const storedUserInfo = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN);
    return storedUserInfo ? JSON.parse(storedUserInfo) : null;
  }

  private clearSessionState(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
    this._userInfo.set(null);
  }
}
