import { Injectable, inject } from '@angular/core';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import {
  ADMIN_SESSION_TOKEN,
  BEARER
} from '../../shared/constants/common.constant';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';

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

  requestAccessToken(token:string): Observable<any> {
    this.setToken('');
    return this.httpClient.post(API_URI.GITHUB_REQUEST_ACCESS , {}, {
      headers: this.createAuthHeader(token)
    });
  }

  clearToken(): void {
    this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
  }

  isAuthenticated(): boolean {
    return !!this.token;
  }

  getAuthHeaders(): HttpHeaders {
    if (!this.token) {
      return new HttpHeaders();
    }

    return this.createAuthHeader(this.token);
  }

  private createAuthHeader(token: string): HttpHeaders {
    return new HttpHeaders({
      Authorization: `${BEARER} ${token}`
    });
  }
}
