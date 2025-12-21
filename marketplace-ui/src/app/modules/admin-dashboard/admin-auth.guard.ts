import { inject, Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { AdminDashboardService } from './admin-dashboard.service';
import { Observable } from 'rxjs/internal/Observable';
import { catchError, map, of } from 'rxjs';
import { AdminAuthService } from './admin-auth.service';

@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  adminService = inject(AdminDashboardService);
  authService = inject(AdminAuthService);

  canActivate(): Observable<boolean> {
    if (!this.authService.isAuthenticated()) {
      return of(true);
    }

    return this.adminService.fetchSyncTaskExecutions().pipe(
      map(() => true),
      catchError(() => {
        this.authService.clearToken();
        return of(true);
      })
    );
  }
}
