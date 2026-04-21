import type { Mock, MockedObject } from 'vitest';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ProductDetailResolver } from './product-detail.resolve';
import { ProductDetailService } from '../../modules/product/product-detail/product-detail.service';
import { LanguageService } from '../services/language/language.service';
import { LoadingService } from '../services/loading/loading.service';
import { ProductService } from '../../modules/product/product.service';
import { CookieService } from 'ngx-cookie-service';
import { RoutingQueryParamService } from '../../shared/services/routing.query.param.service';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import {
  MOCK_PRODUCT_DETAIL,
  MOCK_PRODUCTS
} from '../../shared/mocks/mock-data';
import { TypeOption } from '../../shared/enums/type-option.enum';
import { Language } from '../../shared/enums/language.enum';
import {
  FAVICON_PNG_TYPE,
  OG_DESCRIPTION_KEY,
  OG_IMAGE_KEY,
  OG_IMAGE_PNG_TYPE,
  OG_IMAGE_TYPE_KEY,
  OG_TITLE_KEY,
  NOT_FOUND_ERROR_CODE
} from '../../shared/constants/common.constant';
import { FaviconService } from '../../shared/services/favicon.service';
import { HttpErrorResponse } from '@angular/common/http';

const products = MOCK_PRODUCTS._embedded.products;

describe('ProductDetailResolver', () => {
  let resolver: ProductDetailResolver;
  let productDetailService: MockedObject<ProductDetailService>;
  let meta: MockedObject<Meta>;
  let titleService: MockedObject<Title>;
  let languageService: MockedObject<LanguageService>;
  let loadingService: MockedObject<LoadingService>;
  let productService: MockedObject<ProductService>;
  let cookieService: MockedObject<CookieService>;
  let routingQueryParamService: MockedObject<RoutingQueryParamService>;
  let faviconService: MockedObject<FaviconService>;
  let router: MockedObject<Router>;
  let activatedRoute: ActivatedRoute;
  let routeSnapshot: ActivatedRouteSnapshot;

  beforeEach(() => {
    const productDetailServiceSpy = {
      productId: { set: vi.fn() }
    };
    const metaSpy = {
      updateTag: vi.fn().mockName('Meta.updateTag')
    };
    const titleServiceSpy = {
      setTitle: vi.fn().mockName('Title.setTitle')
    };
    const languageServiceSpy = {
      selectedLanguage: vi.fn().mockName('LanguageService.selectedLanguage')
    };
    const loadingServiceSpy = {
      showLoading: vi.fn().mockName('LoadingService.showLoading')
    };
    const productServiceSpy = {
      getProductDetails: vi.fn().mockName('ProductService.getProductDetails'),
      getBestMatchProductDetailsWithVersion: vi
        .fn()
        .mockName('ProductService.getBestMatchProductDetailsWithVersion'),
      setDefaultVendorImage: vi
        .fn()
        .mockName('ProductService.setDefaultVendorImage')
    };
    const cookieServiceSpy = {
      get: vi.fn().mockName('CookieService.get')
    };
    const routingQueryParamServiceSpy = {
      getDesignerVersionFromSessionStorage: vi
        .fn()
        .mockName(
          'RoutingQueryParamService.getDesignerVersionFromSessionStorage'
        ),
      checkSessionStorageForDesignerVersion: vi
        .fn()
        .mockName(
          'RoutingQueryParamService.checkSessionStorageForDesignerVersion'
        ),
      checkSessionStorageForDesignerEnv: vi
        .fn()
        .mockName('RoutingQueryParamService.checkSessionStorageForDesignerEnv')
    };
    const faviconServiceSpy = {
      setFavicon: vi.fn().mockName('FaviconService.setFavicon')
    };
    const routerSpy = {
      parseUrl: vi.fn().mockName('Router.parseUrl')
    };

    TestBed.configureTestingModule({
      providers: [
        ProductDetailResolver,
        { provide: ProductDetailService, useValue: productDetailServiceSpy },
        { provide: Meta, useValue: metaSpy },
        { provide: Title, useValue: titleServiceSpy },
        { provide: LanguageService, useValue: languageServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy },
        { provide: ProductService, useValue: productServiceSpy },
        { provide: CookieService, useValue: cookieServiceSpy },
        {
          provide: RoutingQueryParamService,
          useValue: routingQueryParamServiceSpy
        },
        {
          provide: FaviconService,
          useValue: faviconServiceSpy
        },
        {
          provide: Router,
          useValue: routerSpy
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: products[0].id },
              data: { productDetail: MOCK_PRODUCT_DETAIL },
              queryParamMap: {
                get: vi.fn().mockReturnValue(null)
              }
            },
            queryParams: of({ type: TypeOption.CONNECTORS }),
            fragment: of('description')
          }
        }
      ]
    });

    resolver = TestBed.inject(ProductDetailResolver);
    activatedRoute = TestBed.inject(ActivatedRoute);
    routeSnapshot = activatedRoute.snapshot;
    router = TestBed.inject(Router) as MockedObject<Router>;
    productDetailService = TestBed.inject(
      ProductDetailService
    ) as MockedObject<ProductDetailService>;
    meta = TestBed.inject(Meta) as MockedObject<Meta>;
    titleService = TestBed.inject(Title) as MockedObject<Title>;
    languageService = TestBed.inject(
      LanguageService
    ) as MockedObject<LanguageService>;
    loadingService = TestBed.inject(
      LoadingService
    ) as MockedObject<LoadingService>;
    faviconService = TestBed.inject(
      FaviconService
    ) as MockedObject<FaviconService>;
  });

  describe('resolve', () => {
    beforeEach(() => {
      vi.spyOn(resolver, 'getProductDetailObservable').mockReturnValue(
        of(MOCK_PRODUCT_DETAIL)
      );
      vi.spyOn(resolver, 'updateProductMetadata');
    });

    it('should set product ID in service', () => {
      resolver.resolve(routeSnapshot).subscribe();

      expect(productDetailService.productId.set).toHaveBeenCalledWith(
        products[0].id
      );
    });

    it('should show loading', () => {
      resolver.resolve(routeSnapshot).subscribe();

      expect(loadingService.showLoading).toHaveBeenCalledWith(
        LoadingComponentId.DETAIL_PAGE
      );
    });

    it('should call getProductDetailObservable with correct product ID', () => {
      resolver.resolve(routeSnapshot).subscribe();

      expect(resolver.getProductDetailObservable).toHaveBeenCalledWith(
        products[0].id,
        null
      );
    });

    it('should update product metadata', () => {
      resolver.resolve(routeSnapshot).subscribe();

      expect(resolver.updateProductMetadata).toHaveBeenCalledWith(
        MOCK_PRODUCT_DETAIL
      );
    });

    it('should return product detail', async () => {
      resolver.resolve(routeSnapshot).subscribe(result => {
        expect(result).toBe(MOCK_PRODUCT_DETAIL);
      });
    });

    it('should only take first emission', () => {
      let emissionCount = 0;
      (resolver.getProductDetailObservable as Mock).mockReturnValue(
        of(MOCK_PRODUCT_DETAIL, { ...MOCK_PRODUCT_DETAIL, id: products[0].id })
      );

      resolver.resolve(routeSnapshot).subscribe(() => {
        emissionCount++;
      });

      expect(emissionCount).toBe(1);
    });

    it('should return UrlTree wrapped in Observable for 404 error', () => {
      const error404 = new HttpErrorResponse({ status: NOT_FOUND_ERROR_CODE });
      const mockUrlTree = {} as UrlTree;
      (resolver.getProductDetailObservable as Mock).mockReturnValue(throwError(() => error404));
      router.parseUrl.mockReturnValue(mockUrlTree);

      resolver.resolve(routeSnapshot).subscribe(result => {
        expect(result).toBe(mockUrlTree);
        expect(router.parseUrl).toHaveBeenCalledWith('/error-page/404');
      });
    });

    it('should complete empty for non-404 errors so navigation cancels without NavigationError', () => {
      const error500 = new HttpErrorResponse({ status: 500 });
      (resolver.getProductDetailObservable as Mock).mockReturnValue(throwError(() => error500));

      let completed = false;
      let thrownError: unknown;
      resolver.resolve(routeSnapshot).subscribe({
        complete: () => { completed = true; },
        error: err => { thrownError = err; }
      });

      // EMPTY from resolver → NavigationCancel (not NavigationError), no redirect
      expect(completed).toBe(true);
      expect(thrownError).toBeUndefined();
      expect(router.parseUrl).not.toHaveBeenCalled();
    });
  });

  describe('updateProductMetadata', () => {
    beforeEach(() => {
      vi.spyOn(resolver, 'updateOGTag');
    });

    it('should set title and update OG tags when product name exists', () => {
      languageService.selectedLanguage.mockReturnValue(Language.DE);
      const productName =
        MOCK_PRODUCT_DETAIL.names[languageService.selectedLanguage()];

      resolver.updateProductMetadata(MOCK_PRODUCT_DETAIL);

      expect(titleService.setTitle).toHaveBeenCalledWith(productName);
      expect(resolver.updateOGTag).toHaveBeenCalledWith(
        OG_TITLE_KEY,
        productName
      );
    });

    it('should update OG description', () => {
      languageService.selectedLanguage.mockReturnValue(Language.DE);

      resolver.updateProductMetadata(MOCK_PRODUCT_DETAIL);

      expect(resolver.updateOGTag).toHaveBeenCalledWith(
        OG_DESCRIPTION_KEY,
        MOCK_PRODUCT_DETAIL.shortDescriptions[
          languageService.selectedLanguage()
        ]
      );
    });

    it('should update OG image and image type', () => {
      resolver.updateProductMetadata(MOCK_PRODUCT_DETAIL);

      expect(resolver.updateOGTag).toHaveBeenCalledWith(
        OG_IMAGE_KEY,
        MOCK_PRODUCT_DETAIL.logoUrl
      );
      expect(resolver.updateOGTag).toHaveBeenCalledWith(
        OG_IMAGE_TYPE_KEY,
        OG_IMAGE_PNG_TYPE
      );
    });

    it('should set favicon with product logo URL', () => {
      resolver.updateProductMetadata(MOCK_PRODUCT_DETAIL);

      expect(faviconService.setFavicon).toHaveBeenCalledWith(
        MOCK_PRODUCT_DETAIL.logoUrl,
        FAVICON_PNG_TYPE
      );
    });
  });

  describe('updateOGTag', () => {
    it('should call meta.updateTag with correct parameters', () => {
      languageService.selectedLanguage.mockReturnValue(Language.DE);
      resolver.updateOGTag(
        OG_TITLE_KEY,
        MOCK_PRODUCT_DETAIL.names[languageService.selectedLanguage()]
      );

      expect(meta.updateTag).toHaveBeenCalledWith({
        property: OG_TITLE_KEY,
        content: MOCK_PRODUCT_DETAIL.names[languageService.selectedLanguage()]
      });
    });
  });
});
