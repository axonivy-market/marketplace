import { HttpClient, HttpContext, HttpHeaders } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { UserInfo } from '../../auth/auth.service';
import { ForwardingError } from '../../core/interceptors/api.interceptor';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { API_URI } from '../../shared/constants/api.constant';
import {
  ADMIN_SESSION_TOKEN,
  BEARER
} from '../../shared/constants/common.constant';

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);
  private readonly _userInfo = signal<UserInfo | null>(null);
  readonly userInfo = this._userInfo.asReadonly();

  constructor() {
    const user = this.loadFromSessionStorage();
    if (user) {
      this._userInfo.set(user);
    } else {
      this.logout();
    }
  }
  loadFromSessionStorage(): UserInfo | null {
    const storedUserInfo = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN);
    return storedUserInfo ? JSON.parse(storedUserInfo) : null;
  }

  logout() {
    this.clearToken();
    this._userInfo.set(null);
  }

  get token(): string | null {
    const storedUserInfo = this.loadFromSessionStorage();
    return storedUserInfo ? storedUserInfo.token : null;
  }

  setUserInfo(userInfo: UserInfo): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, JSON.stringify(userInfo));
    this._userInfo.set(userInfo);
  }

  requestAccessToken(token: string): Observable<UserInfo> {
    this.clearToken();
    return this.httpClient.post<UserInfo>(API_URI.GITHUB_REQUEST_ACCESS,
      { token },
      { context: new HttpContext().set(ForwardingError, true) }
    );
  }

  clearToken(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
  }

  isAuthenticated(): Observable<boolean> {
    return this.httpClient.put<boolean>(API_URI.GITHUB_VALIDATE_TOKEN, {},
      { headers: this.getAuthHeaders(), context: new HttpContext().set(ForwardingError, true) });
  }

  getAuthHeaders(): HttpHeaders {
    if (!this.token) {
      return new HttpHeaders();
    }

    return new HttpHeaders({
      Authorization: `${BEARER} ${this.token}`
    });
  }
}
