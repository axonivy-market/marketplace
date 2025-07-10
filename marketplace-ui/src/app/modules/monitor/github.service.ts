import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';

export interface Repository {
  name: string;
  html_url: string;
  archived: boolean;
  is_template: boolean;
  default_branch: string;
  language: string | null;
  updated_at: string;

}
export interface RepoStatus {
  name: string;
  actionsUrl: string;
  ciBadgeUrl: string;
  devBadgeUrl: string;
  repoUrl: string;
  testReport?: TestReport;
}
export interface TestStep {
  name: string;
  status: 'passed' | 'failed' | 'skipped';
}

export interface TestReport {
  success: boolean;
  summary?: string;
  steps: TestStep[];
}

@Injectable({
  providedIn: 'root'
})
export class GithubService {
  constructor(private http: HttpClient) { }

  getRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(API_URI.GITHUB_REPOS);
  }

  getTestReport(repo: string, workflow: string): Observable<TestReport> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestReport>(url);
  }
}

