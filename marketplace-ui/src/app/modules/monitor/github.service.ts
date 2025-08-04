import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
export interface Repository {
  name: string;
  language: string | null;
  lastUpdated: string;
  focused: boolean;
  testResults?: TestResult[];
}

export interface TestResult {
  workflow: 'CI' | 'DEV' | 'E2E';
  badgeUrl: string;
  results?: TestSummary;
}

export interface TestSummary {
  FAILED: number;
  PASSED: number;
  SKIPPED: number;
}

export interface TestStep {
  name: string;
  status: 'PASSED' | 'FAILED' | 'SKIPPED';
  type: string;
}

@Injectable({
  providedIn: 'root'
})
export class GithubService {
  constructor(private readonly http: HttpClient) { }

  getRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(API_URI.MONITOR_DASHBOARD);
  }

  getTestReport(repo: string, workflow: string): Observable<TestStep> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestStep>(url);
  }
}
