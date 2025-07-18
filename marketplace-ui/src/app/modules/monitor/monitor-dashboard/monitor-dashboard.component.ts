import { Component, inject, OnInit } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule } from '@angular/common';
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
export class MonitoringRedirectComponent implements OnInit {
  repositories: Repository[] = [];
  loading = true;
  error = '';
  isReloading = false;
  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  router = inject(Router);

  ngOnInit(): void {
    this.loadRepositories();
  }

  onSubmit(): void {
    if (this.isReloading) {
      return;
    }

    this.isReloading = true;
    this.githubService.syncGithubRepos().subscribe({
      next: () => {
        console.log('Data reloaded');
      },
      error: (err) => {
        console.error('Reload error:', err);
      },
      complete: () => {
        this.isReloading = false;
      }
    });
  }

  loadRepositories(): void {
    this.loading = true;
    this.githubService.getRepositories().subscribe({
      next: (data) => {
        this.repositories = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  onBadgeClick(repo: string, workflow: string) {
    const upperWorkflow = workflow.toUpperCase();
    this.router.navigate(['/report', repo, upperWorkflow]);
  }
}