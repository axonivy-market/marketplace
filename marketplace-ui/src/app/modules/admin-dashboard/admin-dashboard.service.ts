import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoadingComponent } from '../../core/interceptors/api.interceptor';
import { SyncTaskKey } from '../../shared/constants/admin.constant';
import { API_URI } from '../../shared/constants/api.constant';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { RequestParam } from '../../shared/enums/request-param';
import { SyncTaskStatus } from '../../shared/enums/sync-task-status.enum';
import { SecurityMonitorApiResponse } from '../../shared/models/apis/security-monitor-response.model';
import { SecurityMonitorCriteria } from '../../shared/models/criteria.model';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';

export interface SyncTaskExecution {
  key: SyncTaskKey;
  status?: SyncTaskStatus;
  lastRunDate?: string;
  completedDate?: string;
  message?: string;
}

export interface CustomSortConfig {
  orderedListOfProducts: string[];
  ruleForRemainder: string;
}

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  constructor(private readonly http: HttpClient) {}

  syncProducts(resetSync = false): Observable<SyncTaskExecution> {
    const params = new HttpParams().set(RequestParam.RESET_SYNC, resetSync);
    return this.http.put<SyncTaskExecution>(
      `${API_URI.PRODUCT}/sync`,
      {},
      { params }
    );
  }

  syncOneProduct(
    id: string,
    marketItemPath: string,
    overrideMarketItemPath = false
  ): Observable<SyncTaskExecution> {
    const params = new HttpParams()
      .set(RequestParam.MARKET_ITEM_PATH, marketItemPath)
      .set(RequestParam.OVERRIDE_MARKET_ITEM_PATH, overrideMarketItemPath);
    return this.http.put<SyncTaskExecution>(
      `${API_URI.PRODUCT}/sync/${id}`,
      {},
      { params }
    );
  }

  syncLatestReleasesForProducts(): Observable<void> {
    return this.http.put<void>(
      `${API_URI.PRODUCT_DETAILS}/sync-release-notes`,
      null
    );
  }

  syncZipArtifacts(resetSync = false, productId = ''): Observable<SyncTaskExecution> {
    let params = new HttpParams().set(RequestParam.RESET_SYNC, resetSync);
    if (productId) {
      params = params.set(RequestParam.ID, productId);
    }

    return this.http.put<SyncTaskExecution>(
      `${API_URI.PRODUCT}/zip-sync`,
      {},
      { params }
    );
  }

  syncGithubMonitor(): Observable<string> {
    return this.http.put(
      `${API_URI.SYNC_GITHUB_MONITOR}`,
      {},
      { responseType: 'text' as const }
    );
  }

  syncGithubSecurityMonitor(): Observable<ProductSecurityInfo[]> {
    return this.http.post<ProductSecurityInfo[]>(
      `${API_URI.SYNC_SECURITY_MONITOR}`,
      {}
    );
  }

  fetchSyncTaskExecutions(): Observable<SyncTaskExecution[]> {
    return this.http.get<SyncTaskExecution[]>(API_URI.SYNC_TASK_EXECUTION);
  }

  sortMarketExtensions(
    orderedList: string[],
    remainderRule = 'alphabetically'
  ): Observable<void> {
    const body = {
      orderedListOfProducts: orderedList,
      ruleForRemainder: remainderRule
    };

    return this.http.post<void>(`${API_URI.CUSTOM_SORT}`, body);
  }

  getCustomSort(): Observable<CustomSortConfig> {
    return this.http.get<CustomSortConfig>(`${API_URI.CUSTOM_SORT}`);
  }

  getSecurityDetails(): Observable<ProductSecurityInfo[]> {
    return this.http.get<ProductSecurityInfo[]>(`${API_URI.SECURITY_MONITOR}`, {
      context: new HttpContext().set(
        LoadingComponent,
        LoadingComponentId.SECURITY_MONITOR
      )
    });
  }

  searchSecurityDetails(criteria: SecurityMonitorCriteria):
    Observable<SecurityMonitorApiResponse> {
    let params = new HttpParams()
      .set(RequestParam.PAGE, `${criteria.pageable.page}`)
      .set(RequestParam.SIZE, `${criteria.pageable.size}`)
      .set(RequestParam.SORT, criteria.sortOption)
      .set(RequestParam.SORT_DIRECTION, criteria.sortDirection);

    if (criteria.searchText) {
      params = params.set(RequestParam.SEARCH, criteria.searchText);
    }

    return this.http.get<SecurityMonitorApiResponse>(`${API_URI.SECURITY_MONITOR}`,
      {
        params,
        context: new HttpContext().set(LoadingComponent, LoadingComponentId.SECURITY_MONITOR)
      }
    );
  }
}
