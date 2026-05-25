import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ReleaseLetterCriteria } from '../../../shared/models/criteria.model';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { catchError, Observable, of } from 'rxjs';
import { ReleaseLetterListApiResponse } from '../../../shared/models/apis/release-letter-list-response.model';
import { API_URI } from '../../../shared/constants/api.constant';
import { RequestParam } from '../../../shared/enums/request-param';
import { CachingEnabled, LoadingComponent } from '../../../core/interceptors/api.interceptor';
import { AdminAuthService } from '../admin-auth.service';
import { ReleaseLetter } from '../../../shared/models/release-letter-request.model';
import { ReleaseLetterApiResponse } from '../../../shared/models/apis/release-letter-response.model';
import { ReleaseLetterDraftApiResponse } from '../../../shared/models/apis/release-letter-draft-response.model';

@Injectable({
  providedIn: 'root'
})
export class NewsManagementService {
  private readonly http = inject(HttpClient);
  private readonly adminAuth = inject(AdminAuthService);

  getReleaseLetters(
    releaseLetterCriteria: ReleaseLetterCriteria,
    pageId: string = LoadingComponentId.NEWS_PAGE
  ): Observable<ReleaseLetterListApiResponse> {
    let params = new HttpParams();
    let url: string;

    if (releaseLetterCriteria.nextPageHref) {
      url = releaseLetterCriteria.nextPageHref;
    } else {
      url = releaseLetterCriteria.isReadOnly ? API_URI.RELEASE_LETTERS : `${API_URI.RELEASE_LETTERS}/management`;

      if (releaseLetterCriteria.pageable) {
        params = params
          .set(RequestParam.PAGE, `${releaseLetterCriteria.pageable.page}`)
          .set(RequestParam.SIZE, `${releaseLetterCriteria.pageable.size}`);
      }
    }

    const currentTimeStamp = Date.now().toString();
    params = params.set(RequestParam.TIMESTAMP, currentTimeStamp);

    return this.http
      .get<ReleaseLetterListApiResponse>(url, {
        context: new HttpContext().set(LoadingComponent, pageId).set(CachingEnabled, false),
        headers: this.adminAuth.getAuthHeaders(),
        params
      })
      .pipe(
        catchError(() => {
          const releaseLetterListApiResponse = {} as ReleaseLetterListApiResponse;
          return of(releaseLetterListApiResponse);
        })
      );
  }

  getLatestReleaseLetters(): Observable<ReleaseLetterListApiResponse> {
    let params = new HttpParams();
    const ts = Date.now().toString();
    params = params.set(RequestParam.TIMESTAMP, ts);

    return this.http.get<ReleaseLetterListApiResponse>(`${API_URI.LATEST_RELEASE_LETTERS}`, {
      context: new HttpContext().set(CachingEnabled, false),
      headers: this.adminAuth.getAuthHeaders(),
      params
    });
  }

  createReleaseLetter(releaseLetterRequest: ReleaseLetter): Observable<void> {
    return this.http.post<void>(`${API_URI.RELEASE_LETTERS}`, releaseLetterRequest, {
      headers: this.adminAuth.getAuthHeaders()
    });
  }

  updateReleaseLetter(id: string, releaseLetterRequest: ReleaseLetter): Observable<ReleaseLetterApiResponse> {
    return this.http.put<ReleaseLetterApiResponse>(`${API_URI.RELEASE_LETTERS}/${id}`, releaseLetterRequest, {
      headers: this.adminAuth.getAuthHeaders()
    });
  }

  getReleaseLetterById(id: string): Observable<ReleaseLetterApiResponse> {
    let params = new HttpParams();
    const ts = Date.now().toString();
    params = params.set(RequestParam.TIMESTAMP, ts);

    return this.http.get<ReleaseLetterApiResponse>(`${API_URI.RELEASE_LETTERS}/${id}`, {
      context: new HttpContext()
        .set(LoadingComponent, LoadingComponentId.RELEASE_LETTER_EDIT)
        .set(CachingEnabled, false),
      headers: this.adminAuth.getAuthHeaders(),
      params
    });
  }

  deleteReleaseLetterById(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URI.RELEASE_LETTERS}/${id}`, {
      headers: this.adminAuth.getAuthHeaders()
    });
  }

  saveAsDraft(releaseLetterRequest: ReleaseLetter): Observable<ReleaseLetterDraftApiResponse> {
    return this.http.put<ReleaseLetterDraftApiResponse>(
      `${API_URI.RELEASE_LETTERS}/save-as-draft`,
      releaseLetterRequest,
      {
        headers: this.adminAuth.getAuthHeaders()
      }
    );
  }

  getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId(id: string): Observable<ReleaseLetterDraftApiResponse | null> {
    let params = new HttpParams();
    const currentTimeStamp = Date.now().toString();
    params = params.set(RequestParam.TIMESTAMP, currentTimeStamp);
    return this.http.get<ReleaseLetterDraftApiResponse | null>(`${API_URI.RELEASE_LETTERS}/${id}/draft`, {
      context: new HttpContext().set(CachingEnabled, false),
      headers: this.adminAuth.getAuthHeaders(),
      params
    });
  }
}
