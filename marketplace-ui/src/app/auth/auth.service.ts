import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';
import { catchError, Observable, throwError } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { jwtDecode } from 'jwt-decode';

export interface TokenPayload {
  username: string;
  name: string;
  sub: string;
  exp: number;
}

export interface RequestBody {
  [key: string]: string;
}

export interface TokenResponse {
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly BASE_URL = environment.apiUrl;
  private readonly TOKEN_KEY = 'token';
  private readonly githubAuthUrl = 'https://github.com/login/oauth/authorize';
  private readonly githubAuthCallbackUrl = window.location.origin + environment.githubAuthCallbackPath;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly cookieService: CookieService
  ) {}

  redirectToGitHub(originalUrl: string): void {
    const state = encodeURIComponent(originalUrl);
    const authUrl = `${this.githubAuthUrl}?client_id=${environment.githubClientId}&redirect_uri=${this.githubAuthCallbackUrl}&state=${state}`;
    window.location.href = authUrl;
  }

  handleGitHubCallback(code: string, state: string): void {
    const body = { code };

    this.exchangeCodeForToken(body).subscribe({
      next: response => this.handleTokenResponse(response.token, state),
      error: error => throwError(() => error)
    });
  }

  private exchangeCodeForToken(body: RequestBody): Observable<TokenResponse> {
    const url = `${this.BASE_URL}/auth/github/login`;
    return this.http
      .post<TokenResponse>(url, body)
      .pipe(catchError(error => throwError(() => error)));
  }

  handleTokenResponse(token: string, state: string): void {
    this.setTokenAsCookie(token);
    this.router.navigate([`${state}`], {
      queryParams: { showPopup: 'true' }
    });
  }

  private setTokenAsCookie(token: string): void {
    this.cookieService.set(this.TOKEN_KEY, token, {
      expires: this.extractNumberOfExpiredDay(token),
      path: '/'
    });
  }

  getToken(): string | null {
    const token = this.cookieService.get(this.TOKEN_KEY);
    if (token && !this.isTokenExpired(token)) {
      return token;
    }
    return null;
  }

  private decodeToken(token: string): TokenPayload | null {
    try {
      return jwtDecode(token);
    } catch (error) {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded = this.decodeToken(token);
      if (decoded) {
        if (!decoded?.exp) {
          return false;
        }
        const currentTime = Math.floor(Date.now() / 1000);
        return decoded.exp < currentTime;
      }
    } catch (error) {
      return true;
    }
    return false;
  }

  getDisplayName(): string | null {
    const token = this.getToken();
    if (token && this.decodeToken(token)) {
      const decoded = this.decodeToken(token);
      if (decoded) {
        return decoded.name || decoded.username;
      }
      return decoded;
    }
    return null;
  }

  getUserId(): string | null {
    const token = this.getToken();
    if (token) {
      const decoded = this.decodeToken(token);
      if (decoded) {
        return decoded.sub;
      }
      return null;
    }
    return null;
  }

  private extractNumberOfExpiredDay(token: string): number {
    const exp = this.decodeToken(token)?.exp ?? 0;

    const expDate = new Date(exp * 1000);
    const currentDate = new Date();

    const diffTime = Math.abs(expDate.getTime() - currentDate.getTime());
    return Math.ceil(diffTime / environment.dayInMiliseconds);
  }
}
