import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';
import { RequestParam } from '../../shared/enums/request-param';
import { ProductApiResponse } from '../../shared/models/apis/product-response.model';
import { Criteria } from '../../shared/models/criteria.model';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { VersionData } from '../../shared/models/vesion-artifact.model';
import { ForwardingError, LoadingComponent } from '../../core/interceptors/api.interceptor';
import { VersionAndUrl } from '../../shared/models/version-and-url';
import { API_URI } from '../../shared/constants/api.constant';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { ProductReleasesApiResponse } from '../../shared/models/apis/product-releases-response.model';

@Injectable()
export class ProductService {
  httpClient = inject(HttpClient);

  findProductsByCriteria(criteria: Criteria): Observable<ProductApiResponse> {
    let requestParams = new HttpParams();
    let requestURL = API_URI.PRODUCT;
    if (criteria.nextPageHref) {
      requestURL = criteria.nextPageHref;
    } else {
      requestParams = requestParams
        .set(RequestParam.TYPE, `${criteria.type}`)
        .set(RequestParam.SORT, `${criteria.sort}`)
        .set(RequestParam.KEYWORD, `${criteria.search}`)
        .set(RequestParam.LANGUAGE, `${criteria.language}`)
        .set(RequestParam.PAGE, `${criteria.pageable.page}`)
        .set(RequestParam.SIZE, `${criteria.pageable.size}`)
        .set(
          RequestParam.IS_REST_CLIENT_EDITOR,
          `${criteria.isRESTClientEditor}`
        );
    }
    return this.httpClient.get<ProductApiResponse>(requestURL, {
      params: requestParams,
      context: new HttpContext().set(
        LoadingComponent,
        LoadingComponentId.LANDING_PAGE
      )
    });
  }

  getProductDetailsWithVersion(
    productId: string,
    version: string
  ): Observable<ProductDetail> {
    return this.httpClient.get<ProductDetail>(
      `${API_URI.PRODUCT_DETAILS}/${productId}/${version}`
    );
  }

  getBestMatchProductDetailsWithVersion(
    productId: string,
    version: string
  ): Observable<ProductDetail> {
    return this.httpClient.get<ProductDetail>(
      `${API_URI.PRODUCT_DETAILS}/${productId}/${version}/bestmatch`
    );
  }

  getProductDetails(
    productId: string,
    isShowDevVersion: boolean
  ): Observable<ProductDetail> {
    return this.httpClient
      .get<ProductDetail>(
        `${API_URI.PRODUCT_DETAILS}/${productId}?isShowDevVersion=${isShowDevVersion}`
      );
  }

  sendRequestToProductDetailVersionAPI(
    productId: string,
    showDevVersion: boolean,
    designerVersion: string
  ): Observable<VersionData[]> {
    const url = `${API_URI.PRODUCT_DETAILS}/${productId}/versions`;
    const params = new HttpParams()
      .append('designerVersion', designerVersion)
      .append('isShowDevVersion', showDevVersion);
    return this.httpClient.get<VersionData[]>(url, {
      params,
      context: new HttpContext().set(
        LoadingComponent,
        LoadingComponentId.PRODUCT_VERSION
      )
    });
  }

  sendRequestToGetInstallationCount(productId: string) {
    const url = `${API_URI.PRODUCT_MARKETPLACE_DATA}/installation-count/${productId}`;
    return this.httpClient.get<number>(url);
  }

  sendRequestToGetProductVersionsForDesigner(productId: string, showDevVersion: boolean, designerVersion: string) {
    const url = `${API_URI.PRODUCT_DETAILS}/${productId}/designerversions`;
    const params = new HttpParams().append('designerVersion', designerVersion).append('isShowDevVersion', showDevVersion);
    return this.httpClient.get<VersionAndUrl[]>(url, { params });
  }

  getLatestArtifactDownloadUrl(id: string, version: string, artifact: string) {
    const params = new HttpParams()
      .append('version', version)
      .append('artifact', artifact);
    const url = `${API_URI.PRODUCT_DETAILS}/${id}/artifact`;
    return this.httpClient.get<string>(url, {
      params,
      responseType: 'text' as 'json'
    });
  }

  getProductChangelogs(productId: string): Observable<ProductReleasesApiResponse> {
    const url = `${API_URI.PRODUCT_DETAILS}/${productId}/releases`;

    return this.httpClient.get<ProductReleasesApiResponse>(url, { context: new HttpContext().set(ForwardingError, true) }).pipe(
      catchError(() => {
        const productReleasesApiResponse = {} as ProductReleasesApiResponse;
        return of(productReleasesApiResponse);
      })
    );
  }
}
