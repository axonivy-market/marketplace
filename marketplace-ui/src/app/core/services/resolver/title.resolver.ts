import { inject, Injectable } from '@angular/core';
import {
  ActivatedRoute,
  ActivatedRouteSnapshot,
  Resolve
} from '@angular/router';
import { ProductService } from '../../../modules/product/product.service';
import { map, Observable, of, take, tap } from 'rxjs';
import { CommonUtils } from '../../../shared/utils/common.utils';
import { Meta, Title } from '@angular/platform-browser';
import { Router } from 'express';
import { ProductDetailService } from '../../../modules/product/product-detail/product-detail.service';
import { ROUTER } from '../../../shared/constants/router.constant';
import { DisplayValue } from '../../../shared/models/display-value.model';
import { LanguageService } from '../language/language.service';
import { LoadingService } from '../loading/loading.service';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { ProductDetail } from '../../../shared/models/product-detail.model';
import {
  DEFAULT_VENDOR_IMAGE,
  DEFAULT_VENDOR_IMAGE_BLACK,
  SHOW_DEV_VERSION
} from '../../../shared/constants/common.constant';
import { CookieService } from 'ngx-cookie-service';
import { RoutingQueryParamService } from '../../../shared/services/routing.query.param.service';

@Injectable({ providedIn: 'root' })
export class TitleResolver implements Resolve<ProductDetail> {
  constructor(
    private productDetailService: ProductDetailService,
    private activatedRoute: ActivatedRoute,
    private meta: Meta,
    private titleService: Title,
    private languageService: LanguageService,
    private loadingService: LoadingService,
    private productService: ProductService,
    private cookieService: CookieService,
    private routingQueryParamService: RoutingQueryParamService
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ProductDetail> {
    const productId2 = route.params['id'];
    this.productDetailService.productId.set(productId2);
    // this.titleService.setTitle("TITLE_PLACEHOLDER");
    // this.meta.updateTag({ property: 'og:title', content: "TITLE_PLACEHOLDER" });

    this.loadingService.showLoading(LoadingComponentId.DETAIL_PAGE);
    // return this.getProductDetailObservable(productId2);
    return this.getProductDetailObservable(productId2).pipe(
      tap(productDetail => {
        // ✅ Use it here to update title/meta
        this.updateWebBrowserTitle(productDetail.names);
      })
    );
  }

  //  resolve(route: ActivatedRouteSnapshot): Observable<string> {
  //   const title = 'TITLE FROM RESOLVER';
  //   this.titleService.setTitle(title);
  //   this.meta.updateTag({ property: 'og:title', content: title });
  //   return of(title); // must be Observable or Promise for SSR to wait!
  // }

  updateWebBrowserTitle(names: DisplayValue): void {
    if (names !== undefined) {
      const title = names[this.languageService.selectedLanguage()];
      this.titleService.setTitle(title);
      this.meta.updateTag({ property: 'og:title', content: title });
    }
  }

  getProductDetailObservable(productId: string): Observable<ProductDetail> {
    const isShowDevVersion = CommonUtils.getCookieValue(
      this.cookieService,
      SHOW_DEV_VERSION,
      false
    );
    return this.getProductById(productId, isShowDevVersion);
  }

  getProductById(
    productId: string,
    isShowDevVersion: boolean
  ): Observable<ProductDetail> {
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

  private setDefaultVendorImage(productDetail: ProductDetail): ProductDetail {
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
}
