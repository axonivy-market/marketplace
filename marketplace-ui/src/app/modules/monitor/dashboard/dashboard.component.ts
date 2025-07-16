import { Component, OnInit } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; // Import Router

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  repositories: Repository[] = [];
  loading = true;
  error = '';
  isReloading = false;

  constructor(
    private githubService: GithubService,
    private router: Router 
  ) { }

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
    console.log(`Navigating to report for ${repo}/${workflow}`);
    this.router.navigate(['/report', repo, workflow]);
  }
}