import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AdminAuthService } from './admin-auth.service';

export const REQUEST_ACCESS_PATH = "/request-access";
@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  authService = inject(AdminAuthService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);

  canActivate(): Observable<boolean> {
    if (!isPlatformBrowser(this.platformId)) {
      return of(false);
    }

    return this.authService.isAuthenticated().pipe(
      tap(isAuth => {
        if (!isAuth) {
          this.router.navigate([REQUEST_ACCESS_PATH]);
        }
      }),
      catchError(() => {
        this.router.navigate([REQUEST_ACCESS_PATH]);
        return of(false);
      })
    );
  }
}
