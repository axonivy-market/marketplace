import { Injectable, inject } from '@angular/core';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import {
  ADMIN_SESSION_TOKEN,
  BEARER
} from '../../shared/constants/common.constant';
import { HttpHeaders } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly storageRef = inject(SessionStorageRef);

  get token(): string | null {
    return this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN) ?? null;
  }

  setToken(token: string): void {
    this.storageRef.session?.setItem(ADMIN_SESSION_TOKEN, token);
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

    return new HttpHeaders({
      Authorization: `${BEARER} ${this.token}`
    });
  }
}
