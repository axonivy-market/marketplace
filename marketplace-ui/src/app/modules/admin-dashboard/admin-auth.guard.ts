import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AdminDashboardService } from './admin-dashboard.service';
import { AdminAuthService } from './admin-auth.service';

@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  adminService = inject(AdminDashboardService);
  authService = inject(AdminAuthService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);

  canActivate(): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return false;
    }

    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['request-access']);
      return false;
    }

    return true;
  }
}
