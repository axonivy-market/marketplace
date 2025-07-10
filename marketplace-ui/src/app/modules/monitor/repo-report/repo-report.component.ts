import { Component, OnInit } from '@angular/core';
import { GithubService, TestReport } from '../github.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-repo-report',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './repo-report.component.html',
  styleUrl: './repo-report.component.scss'
})
export class RepoReportComponent implements OnInit {
  report?: TestReport;
  loading = false;
  errorMessage = '';
  repo = '';
  workflow = '';

  constructor(
    private githubService: GithubService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.repo = this.route.snapshot.paramMap.get('repo') ?? '';
    this.workflow = this.route.snapshot.paramMap.get('workflow') ?? '';


    if (this.repo && this.workflow) {
      this.fetchTestReport(this.repo, this.workflow);
    } else {
      this.errorMessage = 'Missing repository or workflow name';
    }
  }

  fetchTestReport(repo: string, workflow: string) {
    this.loading = true;
    this.githubService.getTestReport(repo, workflow).subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load test report';
        this.loading = false;
      }
    });
  }
}
