import { inject, Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AdminDashboardService } from './admin-dashboard.service';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { Observable } from 'rxjs/internal/Observable';
import { ADMIN_SESSION_TOKEN } from '../../shared/constants/common.constant';
import { catchError, map, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  router = inject(Router);
  adminService = inject(AdminDashboardService);
  storageRef = inject(SessionStorageRef);

  canActivate(): Observable<boolean> {
    console.log('AdminAuthGuard#canActivate called');
    const token = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN) ?? '';

    if (!token) {
      return of(true);
    }

    return this.adminService.fetchSyncTaskExecutions().pipe(
      map(() => true),
      catchError(() => {
        this.storageRef.session?.removeItem(ADMIN_SESSION_TOKEN);
        return of(true);
      })
    );
  }
}
