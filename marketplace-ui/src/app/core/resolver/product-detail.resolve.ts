import { Injectable } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { map, Observable, take, tap } from 'rxjs';
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

@Injectable({ providedIn: 'root' })
export class ProductDetailResolver implements Resolve<ProductDetail> {
  constructor(
    private readonly productDetailService: ProductDetailService,
    private readonly meta: Meta,
    private readonly titleService: Title,
    private readonly languageService: LanguageService,
    private readonly loadingService: LoadingService,
    private readonly productService: ProductService,
    private readonly cookieService: CookieService,
    private readonly routingQueryParamService: RoutingQueryParamService
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ProductDetail> {
    const productId = route.params['id'];
    this.productDetailService.productId.set(productId);

    this.loadingService.showLoading(LoadingComponentId.DETAIL_PAGE);
    return this.getProductDetailObservable(productId).pipe(
      take(1),
      tap(productDetail => {
        this.updateProductMetadata(productDetail);
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
    this.updateOGTag(OG_IMAGE_KEY, productDetail.logoUrl);
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
}
