import { Component, inject } from '@angular/core';
import {
  NgbDropdown,
  NgbDropdownButtonItem,
  NgbDropdownItem,
  NgbDropdownMenu,
  NgbDropdownToggle
} from '@ng-bootstrap/ng-bootstrap/dropdown';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';

@Component({
  selector: 'app-github-user-badge',
  imports: [
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

  adminInfo = this.adminAuthService.adminInfo;

  ngOnInit() {
    // if (this.isBrowser) {
    //   console.log("IS BROWSER");
    // }
  }
}
