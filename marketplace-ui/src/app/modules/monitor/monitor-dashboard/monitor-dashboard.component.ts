import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule } from '@ngx-translate/core';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { WorkflowIconPipe } from "../../../shared/pipes/workflow-icon.pipe";
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TranslateModule, BuildStatusEntriesPipe, WorkflowIconPipe],
  templateUrl: './monitor-dashboard.component.html',
  styleUrl: './monitor-dashboard.component.scss'
})
export class MonitoringDashboardComponent implements OnInit {
  repositories: Repository[] = [];
  loading = true;
  error = '';
  isReloading = false;
  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  router = inject(Router);
  platformId = inject(PLATFORM_ID);
  protected mapping = {
    PASSED: { label: 'monitor.dashboard.passed', icon: '✅' },
    FAILED: { label: 'monitor.dashboard.failed', icon: '❌' },
    SKIPPED: { label: 'monitor.dashboard.skipped', icon: '⏩' }
  };

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadRepositories();
    } else {
      this.loading = false;
    }
  }

  loadRepositories(): void {
    this.loading = true;
    this.githubService.getRepositories().subscribe({
      next: data => {
        this.repositories = data;
        this.loading = false;
      },
      error: err => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

getBuildStatuses(results: { [key: string]: number }) {
  return Object.entries(this.mapping)
    .filter(([key]) => results[key])
    .map(([key, meta]) => ({
      label: meta.label,
      icon: meta.icon,
      count: results[key]
    }));
}

  onBadgeClick(repo: string, workflow: string) {
    const upperWorkflow = workflow.toUpperCase();
    this.router.navigate(['/report', repo, upperWorkflow]);
  }
}