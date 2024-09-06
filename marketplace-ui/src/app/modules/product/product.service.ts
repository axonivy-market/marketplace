import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoadingService } from '../../core/services/loading/loading.service';
import { RequestParam } from '../../shared/enums/request-param';
import { ProductApiResponse } from '../../shared/models/apis/product-response.model';
import { Criteria } from '../../shared/models/criteria.model';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { VersionData } from '../../shared/models/vesion-artifact.model';
import { VersionAndUrl } from '../../shared/models/version-and-url';

const PRODUCT_API_URL = 'api/product';
@Injectable()
export class ProductService {
  httpClient = inject(HttpClient);
  loadingService = inject(LoadingService);

  findProductsByCriteria(criteria: Criteria): Observable<ProductApiResponse> {
    let requestParams = new HttpParams();
    let requestURL = PRODUCT_API_URL;
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
        .set(RequestParam.IS_REST_CLIENT_EDITOR, `${criteria.isRESTClientEditor}`);
    }
    return this.httpClient.get<ProductApiResponse>(requestURL, {
      params: requestParams
    });
  }

  getProductDetailsWithVersion(
    productId: string,
    tag: string
  ): Observable<ProductDetail> {
    return this.httpClient.get<ProductDetail>(
      `api/product-details/${productId}/${tag}`
    );
  }

  getBestMatchProductDetailsWithVersion(
    productId: string,
    tag: string
  ): Observable<ProductDetail> {
    return this.httpClient.get<ProductDetail>(
      `api/product-details/${productId}/${tag}/bestmatch`
    );
  }

  getProductDetails(productId: string): Observable<ProductDetail> {
    return this.httpClient.get<ProductDetail>(
      `api/product-details/${productId}`
    );
  }

  sendRequestToProductDetailVersionAPI(
    productId: string,
    showDevVersion: boolean,
    designerVersion: string
  ): Observable<VersionData[]> {
    this.loadingService.show();
    const url = `api/product-details/${productId}/versions`;
    const params = new HttpParams()
      .append('designerVersion', designerVersion)
      .append('isShowDevVersion', showDevVersion);
    return this.httpClient.get<VersionData[]>(url, { params }).pipe(
      tap(() => {
        this.loadingService.hide();
      })
    );
  }

  sendRequestToUpdateInstallationCount(productId: string, designerVersion: string) {
    const url = 'api/product-details/installationcount/' + productId;
    const options = {
      headers: { 'X-Requested-By': 'ivy' },
      params: { 'designerVersion': designerVersion }
    };
    return this.httpClient.put<number>(url, null, options);
  }

  sendRequestToGetProductVersionsForDesigner(productId: string) {
    const url = `api/product-details/${productId}/designerversions`;
    return this.httpClient.get<VersionAndUrl[]>(url, { headers: { 'X-Requested-By': 'ivy' } });
  }
}
