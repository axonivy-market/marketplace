import { inject, Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AdminDashboardService } from './admin-dashboard.service';
import { AdminAuthService } from './admin-auth.service';

@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  adminService = inject(AdminDashboardService);
  authService = inject(AdminAuthService);
  router = inject(Router);

  canActivate(): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['request-access']);
      return false;
    }

    return true;
  }
}
