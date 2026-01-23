import { Injectable, inject } from '@angular/core';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import {
  ADMIN_SESSION_TOKEN,
  BEARER
} from '../../shared/constants/common.constant';
import { HttpClient, HttpContext, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { ForwardingError } from '../../core/interceptors/api.interceptor';

export interface JwtDTO {
  token: string;
}
@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);
  private readonly httpClient = inject(HttpClient);

  get token(): string | null {
    return this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN) ?? null;
  }

  setToken(token: string): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, token);
  }

  requestAccessToken(token: string): Observable<JwtDTO> {
    this.setToken('');
    return this.httpClient.post<JwtDTO>(API_URI.GITHUB_REQUEST_ACCESS,
      { token },
      { context: new HttpContext().set(ForwardingError, true) });
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
