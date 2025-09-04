import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { LoadingComponent } from '../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
export interface Repository {
  name: string;
  htmlUrl: string;
  focused: boolean;
  workflowInformation: WorkflowInformation[];
  testResults: TestResult[];
}

export interface WorkflowInformation {
  workflowType: 'CI' | 'DEV' | 'E2E';
  lastBuilt: Date;
  conclusion: string;
  lastBuiltRun: string;
}

export interface TestResult {
  workflow: 'CI' | 'DEV' | 'E2E';
  results: TestSummary;
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
    return this.http.get<Repository[]>(`${API_URI.MONITOR_DASHBOARD}`, {
      context: new HttpContext().set(
        LoadingComponent,
        LoadingComponentId.MONITORING_DASHBOARD
      )
    });
  }

  getTestReport(repo: string, workflow: string): Observable<TestStep> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestStep>(url);
  }
}
