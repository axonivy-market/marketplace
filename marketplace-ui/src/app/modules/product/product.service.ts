import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LoadingService } from '../../core/services/loading/loading.service';
import { RequestParam } from '../../shared/enums/request-param';
import { ProductApiResponse } from '../../shared/models/apis/product-response.model';
import { Criteria } from '../../shared/models/criteria.model';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { VersionData } from '../../shared/models/vesion-artifact.model';
import { SkipLoading } from '../../core/interceptors/api.interceptor';
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

  getProductDetails(productId: string, isShowDevVersion: boolean): Observable<ProductDetail> {
    return this.httpClient.get<ProductDetail>(
      `api/product-details/${productId}?isShowDevVersion=${isShowDevVersion}`
    );
  }

  sendRequestToProductDetailVersionAPI(
    productId: string,
    showDevVersion: boolean,
    designerVersion: string
  ): Observable<VersionData[]> {
    const url = `api/product-details/${productId}/versions`;
    const params = new HttpParams()
      .append('designerVersion', designerVersion)
      .append('isShowDevVersion', showDevVersion);
    return this.httpClient.get<VersionData[]>(url, {
      params,
      context: new HttpContext().set(SkipLoading, true)
    });
  }

  sendRequestToUpdateInstallationCount(productId: string, designerVersion: string) {
    const url = 'api/product-details/installationcount/' + productId;
    const headers = { 'X-Requested-By': 'ivy' };
    const params = new HttpParams().append('designerVersion', designerVersion);
    return this.httpClient.put<number>(url, null, { headers, params });
  }

  sendRequestToGetProductVersionsForDesigner(productId: string) {
    const url = `api/product-details/${productId}/designerversions`;
    return this.httpClient.get<VersionAndUrl[]>(url, { headers: { 'X-Requested-By': 'ivy' } });
  }
}
