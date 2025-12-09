import { Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { ADMIN_SESSION_TOKEN } from '../../shared/constants/common.constant';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { LoadingComponent } from '../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';

export type SyncJobKey =
  | 'syncProducts'
  | 'syncOneProduct'
  | 'syncLatestReleasesForProducts'
  | 'syncGithubMonitor';

export type SyncJobStatus = 'RUNNING' | 'SUCCESS' | 'FAILED';

export interface SyncJobExecution {
  jobKey: SyncJobKey;
  status: SyncJobStatus;
  triggeredAt?: string;
  completedAt?: string;
  message?: string;
  reference?: string;
}

export interface SyncResponse {
  helpCode?: string;
  helpText?: string;
  messageDetails?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  constructor(
    private readonly http: HttpClient,
    private readonly sessionStorageRef: SessionStorageRef
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token =
      this.sessionStorageRef.session?.getItem(
        ADMIN_SESSION_TOKEN
      ) ?? '';
    if (!token) {
      return new HttpHeaders();
    }
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  syncProducts(resetSync: boolean = false): Observable<SyncResponse> {
    const params = new HttpParams().set('resetSync', String(resetSync));
    return this.http.put<SyncResponse>(
      `${API_URI.PRODUCT}/sync`,
      {},
      {
        params,
        headers: this.getAuthHeaders()
      }
    );
  }

  syncOneProduct(
    id: string,
    marketItemPath: string,
    overrideMarketItemPath?: boolean
  ): Observable<SyncResponse> {
    let params = new HttpParams().set('marketItemPath', marketItemPath);
    if (overrideMarketItemPath != null) {
      params = params.set(
        'overrideMarketItemPath',
        String(overrideMarketItemPath)
      );
    }
    return this.http.put<SyncResponse>(
      `${API_URI.PRODUCT}/sync/${id}`,
      {},
      {
        params,
        headers: this.getAuthHeaders()
      }
    );
  }

  syncLatestReleasesForProducts(): Observable<void> {
    return this.http.get<void>(
      `${API_URI.PRODUCT_DETAILS}/sync-release-notes`,
      {
        headers: this.getAuthHeaders()
      }
    );
  }

  syncGithubMonitor(): Observable<string> {
    return this.http.put(
      `${API_URI.GITHUB_REPORT}/sync`,
      {},
      {
        responseType: 'text',
        headers: this.getAuthHeaders()
      }
    );
  }

  fetchSyncJobExecutions(): Observable<SyncJobExecution[]> {
    return this.http.get<SyncJobExecution[]>(API_URI.SYNC_JOB_EXECUTION, {
      headers: this.getAuthHeaders(),
      context: new HttpContext().set(
                LoadingComponent,
                LoadingComponentId.ADMIN_DASHBOARD
              )
    });
  }

  sortMarketExtensions(
    orderedList: string[],
    remainderRule = 'alphabetically'
  ): Observable<void> {
    const body = {
      orderedListOfProducts: orderedList,
      ruleForRemainder: remainderRule
    };

    return this.http.post<void>(
      `${API_URI.PRODUCT_MARKETPLACE_DATA}/custom-sort`,
      body,
      {
        headers: this.getAuthHeaders()
      }
    );
  }

  getSecurityDetails(): Observable<ProductSecurityInfo[]> {
    return this.http.get<ProductSecurityInfo[]>(`${API_URI.SECURITY_MONITOR}`, {
      headers: this.getAuthHeaders()
    });
  }
}
