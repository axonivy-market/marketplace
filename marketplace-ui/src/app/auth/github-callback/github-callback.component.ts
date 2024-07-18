import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-github-callback',
  standalone: true,
  imports: [],
  template: ''
})
export class GithubCallbackComponent implements OnInit {
  route = inject(ActivatedRoute);
  authService = inject(AuthService);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const state = params['state'];
      if (code && state) {
        this.authService.handleGitHubCallback(code, state);
      }
    });
  }
}
