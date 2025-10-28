import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { LoadingComponent } from '../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { Page } from '../../shared/models/apis/page.model';
import { RequestParam } from '../../shared/enums/request-param';
import { MonitoringCriteria } from '../../shared/models/criteria.model';

export interface Repository {
  repoName: string;
  productId: string;
  htmlUrl: string;
  focused: boolean;
  workflowInformation: WorkflowInformation[];
  testResults: TestResult[];
}

export interface RepositoryPages {
  _embedded: {
    githubRepos: Repository[];
  };
  page?: Page;
}

export interface WorkflowInformation {
  workflowType: 'CI' | 'DEV' | 'E2E';
  lastBuilt: Date;
  conclusion: string;
  lastBuiltRunUrl: string;
  currentWorkflowState: string;
  disabledDate: Date | null;
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
  constructor(private readonly http: HttpClient) {}

  getRepositories(criteria: MonitoringCriteria): Observable<RepositoryPages> {
    const requestParams = new HttpParams()
      .set(RequestParam.PAGE, `${criteria.pageable.page}`)
      .set(RequestParam.SIZE, `${criteria.pageable.size}`)
      .set(RequestParam.SEARCH, `${criteria.search}`)
      .set(RequestParam.IS_FOCUSED, `${criteria.isFocused}`)
      .set(RequestParam.WORK_FLOW_TYPE, `${criteria.workflowType}`)
      .set(RequestParam.SORT_DIRECTION, `${criteria.sortDirection}`);

    const options = {
      params: requestParams,
      context: new HttpContext().set(
        LoadingComponent,
        LoadingComponentId.MONITORING_DASHBOARD
      )
    };

    return this.http.get<RepositoryPages>(`${API_URI.MONITOR_DASHBOARD}`,options);
  }

  getTestReport(repo: string, workflow: string): Observable<TestStep> {
    const url = `${API_URI.GITHUB_REPORT}/${repo}/${workflow}`;
    return this.http.get<TestStep>(url);
  }
}
