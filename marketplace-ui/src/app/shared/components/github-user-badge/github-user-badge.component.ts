import { Component, Inject, inject, Input, PLATFORM_ID } from '@angular/core';
import { GitHubUser } from '../../../auth/auth.service';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-github-user-badge',
  imports: [],
  templateUrl: './github-user-badge.component.html',
  styleUrl: './github-user-badge.component.scss'
})
export class GithubUserBadgeComponent {
  adminAuthService = inject(AdminAuthService);

  adminInfo = this.adminAuthService.adminInfo;

  ngOnInit() {
    // if (this.isBrowser) {
    //   console.log("IS BROWSER");

    // }
  }
}
