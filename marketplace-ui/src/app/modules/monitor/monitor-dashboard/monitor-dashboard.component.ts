import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './monitor-dashboard.component.html',
  styleUrl: './monitor-dashboard.component.scss'
})
export class MonitoringDashboardComponent implements OnInit {
  loading = true;
  error = '';

  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);

  focusedRepos: Repository[] = [];
  standardRepos: Repository[] = [];

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.githubService.getRepositories().subscribe({
        next: res => {
          this.focusedRepos = res.focusedRepos || [];
          this.standardRepos = res.standardRepos || [];
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load repositories';
          this.loading = false;
        }
      });
    }
  }

  getTestCount(
    repo: Repository,
    workflow: string,
    environment: string,
    status: string
  ): number {
    if (!repo.testResults) {
      return 0;
    }
    const result = repo.testResults.find(
      test =>
        test.workflow === workflow.toUpperCase() &&
        test.environment === environment.toUpperCase() &&
        test.status === status.toUpperCase()
    );
    if (!result) {
      return 0;
    }
    return result.count;
  }

  onBadgeClick(repo: string, workflow: string) {
    const upperWorkflow = workflow.toUpperCase();
    this.router.navigate(['/report', repo, upperWorkflow]);
  }

  trackByName(_index: number, repo: Repository) {
    return repo.name;
  }
}