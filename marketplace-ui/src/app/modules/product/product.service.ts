import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoadingService } from '../../core/services/loading/loading.service';
import { RequestParam } from '../../shared/enums/request-param';
import { ProductApiResponse } from '../../shared/models/apis/product-response.model';
import { Criteria } from '../../shared/models/criteria.model';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { VersionData } from '../../shared/models/vesion-artifact.model';

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
        .set(RequestParam.LANGUAGE, `${criteria.language}`);
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

  sendRequestToUpdateInstallationCount(productId: string) {
    const url = 'api/product-details/installationcount/' + productId;
    return this.httpClient.put<number>(url, null, { headers: { 'X-Requested-By': 'ivy' } });
  }
}
