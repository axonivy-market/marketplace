import { Component, Inject, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../auth.service';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-github-callback-admin',
  imports: [],
  template: ''
})
export class GithubCallbackAdminComponent implements OnInit {
  route = inject(ActivatedRoute);
  authService = inject(AuthService);
  isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.route.queryParams.subscribe(params => {
        const code = params['code'];
        const state = params['state'];
        if (code && state) {
          this.authService.handleGitHubAdminCallback(code, state);
        }
      });
    }
  }
}
