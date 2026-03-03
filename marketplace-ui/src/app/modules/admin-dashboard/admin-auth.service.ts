import { Inject, Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import {
  ADMIN_SESSION_TOKEN,
  BEARER
} from '../../shared/constants/common.constant';
import { HttpClient, HttpContext, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { ForwardingError } from '../../core/interceptors/api.interceptor';
import { GitHubUser } from '../../auth/auth.service';
import { isPlatformBrowser } from '@angular/common';

export interface JwtDTO {
  token: string;
  user: GitHubUser;
}

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);
  private readonly _adminInfo = signal<GitHubUser | null>(null);
  readonly adminInfo = this._adminInfo.asReadonly();
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      const user = this.loadFromSession();
      if (user) {
        this._adminInfo.set(user);
      }
    }
  }

  loadFromSession(): GitHubUser | null {
    const stored = sessionStorage.getItem('GITHUB_USER');
    return stored ? JSON.parse(stored) : null;
  }

  setUser(user: GitHubUser) {
    sessionStorage.setItem('GITHUB_USER', JSON.stringify(user));
    this._adminInfo.set(user);
  }

  logout() {
    sessionStorage.removeItem('GITHUB_USER');
    this._adminInfo.set(null);
  }

  get token(): string | null {
    return this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN) ?? null;
  }

  setToken(token: string): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, token);
  }

  requestAccessToken(token: string): Observable<JwtDTO> {
    this.setToken('');
    return this.httpClient.post<JwtDTO>(
      API_URI.GITHUB_REQUEST_ACCESS,
      { token },
      { context: new HttpContext().set(ForwardingError, true) }
    );
  }

  clearToken(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
  }

  isAuthenticated(): Observable<boolean> {
    return this.httpClient.put<boolean>(
      API_URI.GITHUB_VALIDATE_TOKEN,
      {},
      {
        headers: this.getAuthHeaders(),
        context: new HttpContext().set(ForwardingError, true)
      }
    );
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
