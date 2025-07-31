import { Inject, Injectable, Optional, PLATFORM_ID } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, Resolve, Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { catchError, EMPTY, map, Observable, of, take, tap } from 'rxjs';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { ProductDetailService } from '../../modules/product/product-detail/product-detail.service';
import { LanguageService } from '../services/language/language.service';
import { LoadingService } from '../services/loading/loading.service';
import { ProductService } from '../../modules/product/product.service';
import { RoutingQueryParamService } from '../../shared/services/routing.query.param.service';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { CommonUtils } from '../../shared/utils/common.utils';
import {
  DEFAULT_VENDOR_IMAGE,
  DEFAULT_VENDOR_IMAGE_BLACK,
  OG_DESCRIPTION_KEY,
  OG_IMAGE_KEY,
  OG_IMAGE_PNG_TYPE,
  OG_IMAGE_TYPE_KEY,
  OG_TITLE_KEY,
  SHOW_DEV_VERSION
} from '../../shared/constants/common.constant';
import { APP_BASE_HREF, isPlatformServer } from '@angular/common';
import { ROUTER } from '../../shared/constants/router.constant';
import { Request } from 'express';

@Injectable({ providedIn: 'root' })
export class ProductDetailResolver implements Resolve<ProductDetail> {
  constructor(
    private readonly router: Router,
    private readonly productDetailService: ProductDetailService,
    private readonly meta: Meta,
    private readonly titleService: Title,
    private readonly languageService: LanguageService,
    private readonly loadingService: LoadingService,
    private readonly productService: ProductService,
    private readonly cookieService: CookieService,
    private readonly routingQueryParamService: RoutingQueryParamService,
    @Inject(PLATFORM_ID) private readonly platformId: Object,
    @Optional() @Inject(APP_BASE_HREF) private readonly request: any
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    console.error("Product resolver start");
    
    const productId = route.params[ROUTER.ID];
    console.error("Product resolver found product id " + productId);
    if (!productId) {
      this.router.navigate(['/error-page/404']);
      return EMPTY;
    }

    this.productDetailService.productId.set(productId);

    this.loadingService.showLoading(LoadingComponentId.DETAIL_PAGE);
    return this.getProductDetailObservable(productId).pipe(
      take(1),
      tap(productDetail => {
        console.error('Product resolver found productDetail: ', productDetail);
        this.updateProductMetadata(productDetail);
      }),
       catchError(error => {
        console.error('Product resolver error:', error);
        
        // On server-side, return empty data instead of failing
        if (isPlatformServer(this.platformId)) {
          return of(null); // Return null, let client-side handle it
        }
        
        // On client-side, navigate to 404
        this.router.navigate(['/error-page/404']);
        return EMPTY;
      })
    );
  }

  updateProductMetadata(productDetail: ProductDetail): void {
     const baseUrl = this.getBaseUrl();

    const productName = productDetail.names;
    const productShortDescription = productDetail.shortDescriptions;
    const title = productName[this.languageService.selectedLanguage()];
    this.titleService.setTitle(title);
    this.updateOGTag(OG_TITLE_KEY, title);
    this.updateOGTag(
      OG_DESCRIPTION_KEY,
      productShortDescription[this.languageService.selectedLanguage()]
    );
    console.error("Found logo " + productDetail.logoUrl);
    console.error("Found base uRl  " + this.request);
    this.updateOGTag(OG_IMAGE_KEY, this.request + productDetail.logoUrl);
    this.updateOGTag(OG_IMAGE_TYPE_KEY, OG_IMAGE_PNG_TYPE);
  }

  updateOGTag(metaOGkey: string, metaOGContent: string) {
    this.meta.updateTag({
      property: metaOGkey,
      content: metaOGContent
    });
  }

  getProductDetailObservable(productId: string): Observable<ProductDetail> {
    const isShowDevVersion = CommonUtils.getCookieValue(
      this.cookieService,
      SHOW_DEV_VERSION,
      false
    );
    return this.getProductById(productId, isShowDevVersion);
  }

  getProductById(productId: string, isShowDevVersion: boolean): Observable<ProductDetail> {
    const targetVersion =
      this.routingQueryParamService.getDesignerVersionFromSessionStorage();
    let productDetail$: Observable<ProductDetail>;
    if (!targetVersion) {
      productDetail$ = this.productService.getProductDetails(
        productId,
        isShowDevVersion
      );
    } else {
      productDetail$ =
        this.productService.getBestMatchProductDetailsWithVersion(
          productId,
          targetVersion
        );
    }
    return productDetail$.pipe(
      map((response: ProductDetail) => this.setDefaultVendorImage(response))
    );
  }

  setDefaultVendorImage(productDetail: ProductDetail): ProductDetail {
    const { vendorImage, vendorImageDarkMode } = productDetail;

    if (!(productDetail.vendorImage || productDetail.vendorImageDarkMode)) {
      productDetail.vendorImage = DEFAULT_VENDOR_IMAGE_BLACK;
      productDetail.vendorImageDarkMode = DEFAULT_VENDOR_IMAGE;
    } else {
      productDetail.vendorImage = vendorImage || vendorImageDarkMode;
      productDetail.vendorImageDarkMode = vendorImageDarkMode || vendorImage;
    }
    return productDetail;
  }

   private getBaseUrl(): string {
    if (isPlatformServer(this.platformId) && this.request) {
      const protocol = this.request.get('X-Forwarded-Proto') || this.request.protocol;
      const host = this.request.get('Host');
      console.error("getBaseUrl for SSR" + `${protocol}://${host}`);
      return `${protocol}://${host}`;
    }
    return ''; // Client-side, use relative URLs
  }
}
