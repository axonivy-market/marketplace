import { Inject, Injectable, Optional, PLATFORM_ID } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, Resolve, Router, UrlTree } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { map, Observable, take, tap, catchError, of } from 'rxjs';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { ProductDetailService } from '../../modules/product/product-detail/product-detail.service';
import { LanguageService } from '../services/language/language.service';
import { LoadingService } from '../services/loading/loading.service';
import { ProductService } from '../../modules/product/product.service';
import { RoutingQueryParamService } from '../../shared/services/routing.query.param.service';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { CommonUtils } from '../../shared/utils/common.utils';
import {
  FAVICON_PNG_TYPE,
  OG_DESCRIPTION_KEY,
  OG_IMAGE_KEY,
  OG_IMAGE_PNG_TYPE,
  OG_IMAGE_TYPE_KEY,
  OG_TITLE_KEY,
  SHOW_DEV_VERSION,
  NOT_FOUND_ERROR_CODE
} from '../../shared/constants/common.constant';
import { ROUTER } from '../../shared/constants/router.constant';
import {
  API_INTERNAL_URL,
  API_PUBLIC_URL
} from '../../shared/constants/api.constant';
import { isPlatformServer } from '@angular/common';
import { FaviconService } from '../../shared/services/favicon.service';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ProductDetailResolver implements Resolve<ProductDetail | UrlTree> {
  constructor(
    private readonly productDetailService: ProductDetailService,
    private readonly meta: Meta,
    private readonly titleService: Title,
    private readonly languageService: LanguageService,
    private readonly loadingService: LoadingService,
    private readonly productService: ProductService,
    private readonly cookieService: CookieService,
    private readonly routingQueryParamService: RoutingQueryParamService,
    private readonly faviconService: FaviconService,
    private readonly router: Router,
    @Inject(PLATFORM_ID) private readonly platformId: Object,
    @Optional() @Inject(API_INTERNAL_URL) private readonly apiInternalUrl: string,
    @Optional() @Inject(API_PUBLIC_URL) private readonly apiPublicUrl: string
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ProductDetail | UrlTree> {
    const productId = route.params[ROUTER.ID];
    const version = route.queryParamMap.get('version');
    this.productDetailService.productId.set(productId);
    this.routingQueryParamService.checkSessionStorageForDesignerVersion(route.queryParams);
    this.routingQueryParamService.checkSessionStorageForDesignerEnv(route.queryParams);

    this.loadingService.showLoading(LoadingComponentId.DETAIL_PAGE);
    return this.getProductDetailObservable(productId, version).pipe(
      take(1),
      tap(productDetail => {
        if (productDetail && productDetail.names) {
          this.updateProductMetadata(productDetail);
        }
      }),
      catchError((error: HttpErrorResponse) => {
        // SSR-safe: return UrlTree for 404, Angular SSR converts to HTTP 302 redirect
        if (error.status === NOT_FOUND_ERROR_CODE) {
          return of(this.router.parseUrl('error-page/404'));
        }
        // For non-404 errors, rethrow so interceptor/error-bus handles it
        throw error;
      })
    );
  }

  updateProductMetadata(productDetail: ProductDetail): void {
    const productName = productDetail.names;
    const productShortDescription = productDetail.shortDescriptions;
    const title = productName[this.languageService.selectedLanguage()];
    this.titleService.setTitle(title);
    this.updateOGTag(OG_TITLE_KEY, title);
    this.updateOGTag(
      OG_DESCRIPTION_KEY,
      productShortDescription[this.languageService.selectedLanguage()]
    );
    const originalLogoUrl = productDetail.logoUrl;
    let productLogoUrl = '';
    if (isPlatformServer(this.platformId) && this.apiPublicUrl) {
      productLogoUrl =
        this.apiPublicUrl + originalLogoUrl.replace(this.apiInternalUrl, '');
    } else {
      productLogoUrl = originalLogoUrl;
    }
    this.updateOGTag(OG_IMAGE_KEY, productLogoUrl);
    this.updateOGTag(OG_IMAGE_TYPE_KEY, OG_IMAGE_PNG_TYPE);

    this.faviconService.setFavicon(productLogoUrl, FAVICON_PNG_TYPE);
  }

  updateOGTag(metaOGkey: string, metaOGContent: string) {
    this.meta.updateTag({
      property: metaOGkey,
      content: metaOGContent
    });
  }

  getProductDetailObservable(productId: string, version: string | null): Observable<ProductDetail> {
    const isShowDevVersion = CommonUtils.getCookieValue(
      this.cookieService,
      SHOW_DEV_VERSION,
      false
    );
    const productDetail$ = version
      ? this.getProductByIdAndVersion(productId, version)
      : this.getProductById(productId, isShowDevVersion);
    
    return productDetail$.pipe(
      map((response: ProductDetail) => this.productService.setDefaultVendorImage(response))
    );
  }

  private getProductByIdAndVersion(productId: string, version: string): Observable<ProductDetail> {
    return this.productService
      .getProductDetailsWithVersion(productId, version);
  }

  private getProductById(productId: string, isShowDevVersion: boolean): Observable<ProductDetail> {
    const targetVersion =
      this.routingQueryParamService.getDesignerVersionFromSessionStorage();
    if (!targetVersion) {
      return this.productService.getProductDetails(
        productId,
        isShowDevVersion
      );
    }
    return this.productService.getBestMatchProductDetailsWithVersion(
      productId,
      targetVersion
    );
  }
}
