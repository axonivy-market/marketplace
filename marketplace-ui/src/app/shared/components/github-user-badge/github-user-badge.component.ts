import { Component, inject, Input } from '@angular/core';
import { GitHubUser } from '../../../auth/auth.service';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';

@Component({
  selector: 'app-github-user-badge',
  imports: [],
  templateUrl: './github-user-badge.component.html',
  styleUrl: './github-user-badge.component.scss',
})
export class GithubUserBadgeComponent {
  authService = inject(AdminAuthService);

  user = this.authService.user; // signal
}
