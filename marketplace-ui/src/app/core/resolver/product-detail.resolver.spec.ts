import { TestBed } from '@angular/core/testing';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { of } from 'rxjs';
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
import { CommonUtils } from '../../shared/utils/common.utils';
import {
  DEFAULT_VENDOR_IMAGE,
  DEFAULT_VENDOR_IMAGE_BLACK,
  OG_DESCRIPTION_KEY,
  OG_IMAGE_KEY,
  OG_IMAGE_PNG_TYPE,
  OG_IMAGE_TYPE_KEY,
  OG_TITLE_KEY
} from '../../shared/constants/common.constant';

const products = MOCK_PRODUCTS._embedded.products;

describe('ProductDetailResolver', () => {
  let resolver: ProductDetailResolver;
  let productDetailService: jasmine.SpyObj<ProductDetailService>;
  let meta: jasmine.SpyObj<Meta>;
  let titleService: jasmine.SpyObj<Title>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let loadingService: jasmine.SpyObj<LoadingService>;
  let productService: jasmine.SpyObj<ProductService>;
  let cookieService: jasmine.SpyObj<CookieService>;
  let routingQueryParamService: jasmine.SpyObj<RoutingQueryParamService>;
  let activatedRoute: ActivatedRoute;
  let routeSnapshot: ActivatedRouteSnapshot;

  beforeEach(() => {
    const productDetailServiceSpy = jasmine.createSpyObj(
      'ProductDetailService',
      [],
      {
        productId: { set: jasmine.createSpy('set') }
      }
    );
    const metaSpy = jasmine.createSpyObj('Meta', ['updateTag']);
    const titleServiceSpy = jasmine.createSpyObj('Title', ['setTitle']);
    const languageServiceSpy = jasmine.createSpyObj('LanguageService', [
      'selectedLanguage'
    ]);
    const loadingServiceSpy = jasmine.createSpyObj('LoadingService', [
      'showLoading'
    ]);
    const productServiceSpy = jasmine.createSpyObj('ProductService', [
      'getProductDetails',
      'getBestMatchProductDetailsWithVersion'
    ]);
    const cookieServiceSpy = jasmine.createSpyObj('CookieService', ['get']);
    const routingQueryParamServiceSpy = jasmine.createSpyObj(
      'RoutingQueryParamService',
      ['getDesignerVersionFromSessionStorage']
    );

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
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: products[0].id },
              data: { productDetail: MOCK_PRODUCT_DETAIL }
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
    productDetailService = TestBed.inject(
      ProductDetailService
    ) as jasmine.SpyObj<ProductDetailService>;
    meta = TestBed.inject(Meta) as jasmine.SpyObj<Meta>;
    titleService = TestBed.inject(Title) as jasmine.SpyObj<Title>;
    languageService = TestBed.inject(
      LanguageService
    ) as jasmine.SpyObj<LanguageService>;
    loadingService = TestBed.inject(
      LoadingService
    ) as jasmine.SpyObj<LoadingService>;
    productService = TestBed.inject(
      ProductService
    ) as jasmine.SpyObj<ProductService>;
    cookieService = TestBed.inject(
      CookieService
    ) as jasmine.SpyObj<CookieService>;
    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as jasmine.SpyObj<RoutingQueryParamService>;
  });

  describe('resolve', () => {
    beforeEach(() => {
      spyOn(resolver, 'getProductDetailObservable').and.returnValue(
        of(MOCK_PRODUCT_DETAIL)
      );
      spyOn(resolver, 'updateProductMetadata');
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
        products[0].id
      );
    });

    it('should update product metadata', () => {
      resolver.resolve(routeSnapshot).subscribe();

      expect(resolver.updateProductMetadata).toHaveBeenCalledWith(
        MOCK_PRODUCT_DETAIL
      );
    });

    it('should return product detail', done => {
      resolver.resolve(routeSnapshot).subscribe(result => {
        expect(result).toBe(MOCK_PRODUCT_DETAIL);
        done();
      });
    });

    it('should only take first emission', () => {
      let emissionCount = 0;
      (resolver.getProductDetailObservable as jasmine.Spy).and.returnValue(
        of(MOCK_PRODUCT_DETAIL, { ...MOCK_PRODUCT_DETAIL, id: products[0].id })
      );

      resolver.resolve(routeSnapshot).subscribe(() => {
        emissionCount++;
      });

      expect(emissionCount).toBe(1);
    });
  });

  describe('updateProductMetadata', () => {
    beforeEach(() => {
      spyOn(resolver, 'updateOGTag');
    });

    it('should set title and update OG tags when product name exists', () => {
      languageService.selectedLanguage.and.returnValue(Language.DE);
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
      languageService.selectedLanguage.and.returnValue(Language.DE);

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
  });

  describe('updateOGTag', () => {
    it('should call meta.updateTag with correct parameters', () => {
      languageService.selectedLanguage.and.returnValue(Language.DE);
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

  describe('getProductDetailObservable', () => {
    beforeEach(() => {
      spyOn(CommonUtils, 'getCookieValue').and.returnValue(false);
      spyOn(resolver, 'getProductById').and.returnValue(
        of(MOCK_PRODUCT_DETAIL)
      );
    });

    it('should call getProductById with correct parameters', () => {
      (CommonUtils.getCookieValue as jasmine.Spy).and.returnValue(true);

      resolver.getProductDetailObservable(products[0].id);

      expect(resolver.getProductById).toHaveBeenCalledWith(
        products[0].id,
        true
      );
    });
  });

  describe('getProductById', () => {
    beforeEach(() => {
      spyOn(resolver, 'setDefaultVendorImage').and.returnValue(
        MOCK_PRODUCT_DETAIL
      );
    });

    it('should call getProductDetails when no target version', () => {
      routingQueryParamService.getDesignerVersionFromSessionStorage.and.returnValue(
        ''
      );
      productService.getProductDetails.and.returnValue(of(MOCK_PRODUCT_DETAIL));

      resolver.getProductById(products[0].id, false).subscribe();

      expect(productService.getProductDetails).toHaveBeenCalledWith(
        products[0].id,
        false
      );
      expect(
        productService.getBestMatchProductDetailsWithVersion
      ).not.toHaveBeenCalled();
    });

    it('should call getBestMatchProductDetailsWithVersion when target version exists', () => {
      const targetVersion = '1.0';
      routingQueryParamService.getDesignerVersionFromSessionStorage.and.returnValue(
        targetVersion
      );
      productService.getBestMatchProductDetailsWithVersion.and.returnValue(
        of(MOCK_PRODUCT_DETAIL)
      );

      resolver.getProductById(products[0].id, true).subscribe();

      expect(
        productService.getBestMatchProductDetailsWithVersion
      ).toHaveBeenCalledWith(products[0].id, targetVersion);
      expect(productService.getProductDetails).not.toHaveBeenCalled();
    });

    it('should apply setDefaultVendorImage transformation', () => {
      const targetVersion = '1.0';
      routingQueryParamService.getDesignerVersionFromSessionStorage.and.returnValue(
        targetVersion
      );

      productService.getBestMatchProductDetailsWithVersion.and.returnValue(
        of(MOCK_PRODUCT_DETAIL)
      );

      resolver.getProductById(products[0].id, false).subscribe();

      expect(resolver.setDefaultVendorImage).toHaveBeenCalledWith(
        MOCK_PRODUCT_DETAIL
      );
    });

    it('should return transformed product detail', done => {
      routingQueryParamService.getDesignerVersionFromSessionStorage.and.returnValue(
        ''
      );
      productService.getProductDetails.and.returnValue(of(MOCK_PRODUCT_DETAIL));

      resolver.getProductById(products[0].id, false).subscribe(result => {
        expect(result).toBe(MOCK_PRODUCT_DETAIL);
        done();
      });
    });
  });

  describe('setDefaultVendorImage', () => {
    it('should set default images when both vendor images are missing', () => {
      const productDetailWithoutVendorImages = {
        ...MOCK_PRODUCT_DETAIL,
        vendorImage: '',
        vendorImageDarkMode: ''
      };

      const result = resolver.setDefaultVendorImage(
        productDetailWithoutVendorImages
      );

      expect(result.vendorImage).toBe(DEFAULT_VENDOR_IMAGE_BLACK);
      expect(result.vendorImageDarkMode).toBe(DEFAULT_VENDOR_IMAGE);
    });

    it('should use vendorImage for dark mode when dark mode image is missing', () => {
      const productDetailWithoutDarkMode = {
        ...MOCK_PRODUCT_DETAIL,
        vendorImageDarkMode: ''
      };

      const result = resolver.setDefaultVendorImage(
        productDetailWithoutDarkMode
      );

      expect(result.vendorImage).toBe(MOCK_PRODUCT_DETAIL.vendorImage);
      expect(result.vendorImageDarkMode).toBe(MOCK_PRODUCT_DETAIL.vendorImage);
    });

    it('should use dark mode image for regular when regular image is missing', () => {
      const productDetailWithoutRegularImage = {
        ...MOCK_PRODUCT_DETAIL,
        vendorImage: ''
      };

      const result = resolver.setDefaultVendorImage(
        productDetailWithoutRegularImage
      );

      expect(result.vendorImage).toBe(MOCK_PRODUCT_DETAIL.vendorImageDarkMode);
      expect(result.vendorImageDarkMode).toBe(
        MOCK_PRODUCT_DETAIL.vendorImageDarkMode
      );
    });

    it('should keep original images when both are present', () => {
      const result = resolver.setDefaultVendorImage(MOCK_PRODUCT_DETAIL);

      expect(result.vendorImage).toBe(MOCK_PRODUCT_DETAIL.vendorImage);
      expect(result.vendorImageDarkMode).toBe(
        MOCK_PRODUCT_DETAIL.vendorImageDarkMode
      );
    });
  });
});
