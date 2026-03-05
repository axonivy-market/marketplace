import { Component, inject } from '@angular/core';
import {
  NgbDropdown,
  NgbDropdownButtonItem,
  NgbDropdownItem,
  NgbDropdownMenu,
  NgbDropdownToggle
} from '@ng-bootstrap/ng-bootstrap/dropdown';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../../core/services/theme/theme.service';

@Component({
  selector: 'app-github-user-badge',
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule,
    NgbDropdown,
    NgbDropdownToggle,
    NgbDropdownMenu,
    NgbDropdownItem,
    NgbDropdownButtonItem
  ],
  templateUrl: './github-user-badge.component.html',
  styleUrl: './github-user-badge.component.scss'
})
export class GithubUserBadgeComponent {
  adminAuthService = inject(AdminAuthService);
  router = inject(Router);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);

  adminInfo = this.adminAuthService.adminInfo;

  logout() {
    this.adminAuthService.logout();
    this.router.navigate(['/']);
  }
}
