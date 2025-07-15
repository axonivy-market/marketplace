import { Component, OnInit } from '@angular/core';
import { GithubService, Repository } from '../github.service';
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
  repositories: Repository[] = [];
  loading = true;
  error = '';

  constructor(private githubService: GithubService) { }

  ngOnInit(): void {
    this.loadRepositories();
  }

  onSubmit(): void {
    this.githubService.syncGithubRepos().subscribe({
      next: () => {
        console.log('Sync successful');
        this.loadRepositories(); 
      },
      error: (err) => {
        console.error('Sync failed', err);
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
}