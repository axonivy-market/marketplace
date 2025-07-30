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
  focusedRepo: boolean;
  testResults?: TestResult[];
}

export interface ReposResponseModel {
  focusedRepos: Repository[];
  standardRepos: Repository[];
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

  getRepositories(): Observable<ReposResponseModel> {
    return this.http.get<ReposResponseModel>(API_URI.MONITOR_DASHBOARD);
  }

  getTestReport(repo: string, workflow: string): Observable<TestStep> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestStep>(url);
  }
}