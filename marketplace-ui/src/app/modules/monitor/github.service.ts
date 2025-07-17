import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';

export interface Repository {
  name: string;
  html_url: string;
  language: string | null;
  updated_at: string;
  ciBadgeUrl: string;
  devBadgeUrl: string;
  lastUpdated: Date;
  testStepsModels: TestStep[];
}

export interface TestStep {
  name: string;
  status: 'PASSED' | 'FAILED' | 'SKIPPED';
  type: string;
  testType: string;
}


@Injectable({
  providedIn: 'root'
})
export class GithubService {
  constructor(private http: HttpClient) { }

  getRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(API_URI.GITHUB_REPOS);
  }

  syncGithubRepos(): Observable<Repository[]> {
    return this.http.get<Repository[]>(API_URI.SYNC_GITHUB_REPOS);
  }

  getTestReport(repo: string, workflow: string): Observable<TestStep> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestStep>(url);
  }
}

