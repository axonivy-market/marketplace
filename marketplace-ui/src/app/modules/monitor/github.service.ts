import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
export interface Repository {
  name: string;
  htmlUrl: string;
  language: string | null;
  lastUpdated: string;
  ciBadgeUrl: string;
  devBadgeUrl: string;
  testResults?: TestResult[];
}

export interface TestResult {
  environment: 'ALL' | 'REAL' | 'MOCK';
  workflow: 'CI' | 'DEV';
  count: number;
  status: 'PASSED' | 'FAILED';
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
  constructor(private readonly http: HttpClient) { }

  getStandardRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(API_URI.MONITOR_DASHBOARD_STANDARD);
  }
  getFocusedRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(API_URI.MONITOR_DASHBOARD_FOCUSED);
  }
  

  getTestReport(repo: string, workflow: string): Observable<TestStep> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestStep>(url);
  }
}