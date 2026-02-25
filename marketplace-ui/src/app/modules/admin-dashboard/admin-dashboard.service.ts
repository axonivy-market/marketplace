import { ReleaseLetterCriteria } from './../../shared/models/criteria.model';
import { Injectable, signal, WritableSignal } from '@angular/core';
import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { catchError, Observable, of } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { AdminAuthService } from './admin-auth.service';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import {
  ForwardingError,
  LoadingComponent
} from '../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { RequestParam } from '../../shared/enums/request-param';
import { SyncTaskStatus } from '../../shared/enums/sync-task-status.enum';
import { ReleaseLetterListApiResponse } from '../../shared/models/apis/release-letter-list-response.model';
import { ReleaseLetterApiResponse } from '../../shared/models/apis/release-letter-response.model';
import { ReleaseLetter } from '../../shared/models/release-letter-request.model';
import { debug } from 'console';

export type SyncTaskKey =
  | 'syncProducts'
  | 'syncOneProduct'
  | 'syncLatestReleasesForProducts'
  | 'syncGithubMonitor';

export interface SyncTaskExecution {
  key: SyncTaskKey;
  status?: SyncTaskStatus;
  triggeredAt?: string;
  completedAt?: string;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  constructor(
    private readonly http: HttpClient,
    private readonly adminAuth: AdminAuthService
  ) {}

  syncProducts(resetSync = false): Observable<SyncTaskExecution> {
    const params = new HttpParams().set(RequestParam.RESET_SYNC, resetSync);
    return this.http.put<SyncTaskExecution>(
      `${API_URI.PRODUCT}/sync`,
      {},
      {
        params,
        headers: this.adminAuth.getAuthHeaders()
      }
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
      {
        params,
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  syncLatestReleasesForProducts(): Observable<void> {
    return this.http.get<void>(
      `${API_URI.PRODUCT_DETAILS}/sync-release-notes`,
      {
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  syncGithubMonitor(): Observable<string> {
    return this.http.put(
      `${API_URI.SYNC_GITHUB_MONITOR}`,
      {},
      {
        responseType: 'text' as const,
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  fetchSyncTaskExecutions(): Observable<SyncTaskExecution[]> {
    return this.http.get<SyncTaskExecution[]>(API_URI.SYNC_TASK_EXECUTION, {
      headers: this.adminAuth.getAuthHeaders(),
      context: new HttpContext().set(ForwardingError, true)
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
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  getSecurityDetails(): Observable<ProductSecurityInfo[]> {
    return this.http.get<ProductSecurityInfo[]>(`${API_URI.SECURITY_MONITOR}`, {
      headers: this.adminAuth.getAuthHeaders(),
      context: new HttpContext().set(
        LoadingComponent,
        LoadingComponentId.SECURITY_MONITOR
      )
    });
  }

  getReleaseLettersWithoutPaging(): Observable<ReleaseLetterListApiResponse> {
    return this.http.get<ReleaseLetterListApiResponse>(
      `${API_URI.RELEASE_LETTERS_WITHOUT_PAGINATION}`,
      {
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  getReleaseLetters(
    releaseLetterCriteria: ReleaseLetterCriteria,
    pageId: String = LoadingComponentId.NEWS_PAGE
  ): Observable<ReleaseLetterListApiResponse> {
    let params = new HttpParams();
    let url = '';

     if (releaseLetterCriteria.nextPageHref) {
      url = releaseLetterCriteria.nextPageHref;
    } else {
      url = `${API_URI.RELEASE_LETTERS}`;
      
      if (releaseLetterCriteria.pageable) {
        params = params
          .set(RequestParam.PAGE, `${releaseLetterCriteria.pageable.page}`)
          .set(RequestParam.SIZE, `${releaseLetterCriteria.pageable.size}`);
      }
    }
    return this.http
          .get<ReleaseLetterListApiResponse>(url, {
            context: new HttpContext().set(
              LoadingComponent,
              pageId
            ),
            params
          })
          .pipe(
            catchError(() => {
              const releaseLetterListApiResponse = {} as ReleaseLetterListApiResponse;
              return of(releaseLetterListApiResponse);
            })
          );
  }

  getActiveReleaseLetters(): Observable<ReleaseLetterListApiResponse> {
    return this.http.get<ReleaseLetterListApiResponse>(
      `${API_URI.ACTIVE_RELEASE_LETTERS}`,
      {
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  createReleaseLetter(
    releaseLetterRequest: ReleaseLetter
  ): Observable<ReleaseLetterApiResponse> {
    return this.http.post<ReleaseLetterApiResponse>(
      `${API_URI.RELEASE_LETTERS}`,
      releaseLetterRequest,
      {
        headers: this.adminAuth.getAuthHeaders(),
        context: new HttpContext().set(ForwardingError, true)
      }
    );
  }

  updateReleaseLetter(
    selectedSprint: string,
    releaseLetterRequest: ReleaseLetter
  ): Observable<ReleaseLetterApiResponse> {
    return this.http.put<ReleaseLetterApiResponse>(
      `${API_URI.RELEASE_LETTERS}/sprint/${selectedSprint}`,
      releaseLetterRequest,
      {
        headers: this.adminAuth.getAuthHeaders(),
        context: new HttpContext().set(ForwardingError, true)
      }
    );
  }

  getRelaseLetterBySprint(
    sprint: string
  ): Observable<ReleaseLetterApiResponse> {
    return this.http.get<ReleaseLetterApiResponse>(
      `${API_URI.RELEASE_LETTERS}/sprint/${sprint}`,
      {
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  deleteReleaseLetterBySprint(sprint: string): Observable<void> {
    return this.http.delete<void>(
      `${API_URI.RELEASE_LETTERS}/sprint/${sprint}`,
      {
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }
}
