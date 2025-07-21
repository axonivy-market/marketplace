import { Component, inject, OnInit } from '@angular/core';
import { GithubService, TestStep } from '../github.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';

@Component({
  selector: 'app-repo-report',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './repo-report.component.html',
  styleUrl: './repo-report.component.scss'
})
export class RepoReportComponent implements OnInit {
  report: TestStep[] = [];
  loading = false;
  errorMessage = '';
  repo = '';
  workflow = '';
  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  route = inject(ActivatedRoute);


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
    this.errorMessage = '';
    this.githubService.getTestReport(repo, workflow).subscribe({
      next: (data) => {
        console.log('Test report data:', data);
        this.report = Array.isArray(data) ? data : [data];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching test report:', error);
        this.errorMessage = 'Failed to load test report';
        this.loading = false;
      }
    });
  }

}