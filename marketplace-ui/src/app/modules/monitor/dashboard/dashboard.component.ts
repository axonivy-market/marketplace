import { Component, OnInit } from '@angular/core';
import { GithubService, Repository, RepoStatus } from '../github.service';
import { GITHUB_MARKET_ORG_URL } from '../../../shared/constants/common.constant';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule,],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  repos: RepoStatus[] = [];
  loading = true;
  errorMessage = '';
  readonly org = 'axonivy-market';

  private readonly ignoredRepos = [
    'market-up2date-keeper',
    'market.axonivy.com',
    'market-monitor',
    'market'
  ];

  constructor(private githubService: GithubService, private router: Router) { }

  ngOnInit(): void {
    this.githubService.getRepositories().subscribe({
      next: (repositories: Repository[]) => {
        this.repos = repositories
          .filter(repo =>
            !this.ignoredRepos.includes(repo.name) &&
            !repo.archived &&
            !repo.is_template &&
            repo.default_branch === 'master' &&
            repo.language !== null
          )
          .map(repo => this.mapToRepoStatus(repo));
        this.loading = false;
      },
      error: (err: Error) => {
        this.errorMessage = err.message;
        this.loading = false;
      }
    });
  }

  private mapToRepoStatus(repo: Repository): RepoStatus {
    const base = GITHUB_MARKET_ORG_URL + `/${repo.name}/actions/workflows`;
    return {
      name: repo.name,
      repoUrl: repo.html_url,
      actionsUrl: GITHUB_MARKET_ORG_URL + `/${repo.name}/actions`,
      ciBadgeUrl: `${base}/ci.yml/badge.svg`,
      devBadgeUrl: `${base}/dev.yml/badge.svg`
    };
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).style.display = 'none';
  }
  
  loadReport(repo: string, workflow: string): void {
    this.router.navigate(['/report', repo, workflow]);
  }
}