
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductDetailInformationTabComponent } from './product-detail-information-tab.component';
import { of, throwError } from 'rxjs';
import { SimpleChange, SimpleChanges } from '@angular/core';
import { ProductDetailService } from '../product-detail.service';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MOCK_EXTERNAL_DOCUMENT } from '../../../../shared/mocks/mock-data';
import { ActivatedRoute } from '@angular/router';

const TEST_ID = 'portal';
const TEST_VERSION = '10.0.0';
const TEST_VERSION_PARAM = 'Version 10.0.0';
const TEST_ARTIFACT_NAME = 'Portal Guide';
const TEST_DOC_URL = '/market-cache/portal/portal-guide/10.0.0/doc/index.html';
const SHIELDS_BADGE_BASE_URL = 'https://img.shields.io/github/actions/workflow/status';
const SHIELDS_WORKFLOW = 'ci.yml';
const BRANCH = 'master';

describe('ProductDetailInformationTabComponent', () => {
  let component: ProductDetailInformationTabComponent;
  let fixture: ComponentFixture<ProductDetailInformationTabComponent>;
  let productDetailService: jasmine.SpyObj<ProductDetailService>;
  let mockVersion: string | null = TEST_VERSION_PARAM;

  beforeEach(async () => {
    const productDetailServiceSpy = jasmine.createSpyObj(
      'ProductDetailService',
      ['getExternalDocumentForProductByVersion', 'getBestMatchVersion']
    );
    mockVersion = TEST_VERSION_PARAM;
    await TestBed.configureTestingModule({
      imports: [
        ProductDetailInformationTabComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: ProductDetailService, useValue: productDetailServiceSpy },
        {
          provide: ActivatedRoute,
          useFactory: () => ({
            snapshot: {
              queryParamMap: {
                get: (key: string) => (key === 'version' ? mockVersion : null)
              }
            }
          })
        },
        LanguageService,
        TranslateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetailInformationTabComponent);
    component = fixture.componentInstance;
    productDetailService = TestBed.inject(ProductDetailService) as jasmine.SpyObj<ProductDetailService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set externalDocumentLink and displayExternalDocName on valid version change', () => {
    productDetailService.getBestMatchVersion.and.returnValue(of(TEST_VERSION));
    productDetailService.getExternalDocumentForProductByVersion.and.returnValue(of({ ...MOCK_EXTERNAL_DOCUMENT }));

    component.productDetail = { id: TEST_ID, newestReleaseVersion: TEST_VERSION } as ProductDetail;
    component.selectedVersion = TEST_VERSION;
    const changes: SimpleChanges = {
      selectedVersion: {
        currentValue: TEST_VERSION,
        previousValue: '8.0.0',
        firstChange: false,
        isFirstChange: () => false
      },
      productDetail: {
        currentValue: component.productDetail,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    };

    component.ngOnChanges(changes);

    expect(productDetailService.getExternalDocumentForProductByVersion).toHaveBeenCalledWith(TEST_ID, TEST_VERSION);
    expect(component.externalDocumentLink).toBe(TEST_DOC_URL);
    expect(component.displayExternalDocName).toBe(TEST_ARTIFACT_NAME);
  });

    it('should set externalDocumentLink and displayExternalDocName on null response', () => {
      productDetailService.getBestMatchVersion.and.returnValue(of(TEST_VERSION));
      (
        productDetailService.getExternalDocumentForProductByVersion as jasmine.Spy
      ).and.returnValue(of(null));
      component.productDetail = {
        id: TEST_ID,
        newestReleaseVersion: TEST_VERSION
      } as ProductDetail;
      component.selectedVersion = TEST_VERSION;
      const changes: SimpleChanges = {
        selectedVersion: {
          currentValue: TEST_VERSION,
          previousValue: '8.0.0',
          firstChange: false,
          isFirstChange: () => false
        },
        productDetail: {
          currentValue: component.productDetail,
          previousValue: null,
          firstChange: true,
          isFirstChange: () => true
        }
      };

      component.ngOnChanges(changes);

      expect(
        productDetailService.getExternalDocumentForProductByVersion
      ).toHaveBeenCalledWith(TEST_ID, TEST_VERSION);
      expect(component.externalDocumentLink).toBe('');
      expect(component.displayExternalDocName).toBe('');
    });

  it('should set externalDocumentLink and displayExternalDocName on error', () => {
    productDetailService.getBestMatchVersion.and.returnValue(of(TEST_VERSION));
    productDetailService.getExternalDocumentForProductByVersion.and.returnValue(
      throwError(() => new Error('Network error'))
    );
    component.productDetail = {
      id: TEST_ID,
      newestReleaseVersion: TEST_VERSION
    } as ProductDetail;
    component.selectedVersion = TEST_VERSION;
    const changes: SimpleChanges = {
      selectedVersion: {
        currentValue: TEST_VERSION,
        previousValue: '8.0.0',
        firstChange: false,
        isFirstChange: () => false
      },
      productDetail: {
        currentValue: component.productDetail,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    };

    component.ngOnChanges(changes);

    expect(
      productDetailService.getExternalDocumentForProductByVersion
    ).toHaveBeenCalledWith(TEST_ID, TEST_VERSION);
    expect(component.externalDocumentLink).toBe('');
    expect(component.displayExternalDocName).toBe('');
  });

  it('should set externalDocumentLink and displayExternalDocName on getBestMatch error', () => {
    productDetailService.getBestMatchVersion.and.returnValue(
      throwError(() => new Error('Network error'))
    );
    component.productDetail = {
      id: TEST_ID,
      newestReleaseVersion: TEST_VERSION
    } as ProductDetail;
    component.selectedVersion = TEST_VERSION;
    const changes: SimpleChanges = {
      selectedVersion: {
        currentValue: TEST_VERSION,
        previousValue: '8.0.0',
        firstChange: false,
        isFirstChange: () => false
      },
      productDetail: {
        currentValue: component.productDetail,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    };

    component.ngOnChanges(changes);

    expect(productDetailService.getBestMatchVersion).toHaveBeenCalled();
    expect(component.externalDocumentLink).toBe('');
    expect(component.displayExternalDocName).toBe('');
  });

  it('should not set externalDocumentLink if version is invalid', () => {
    productDetailService.getBestMatchVersion.and.returnValue(of(TEST_VERSION));
    mockVersion = null;
    component.productDetail = { id: TEST_ID, newestReleaseVersion: '' } as ProductDetail;
    component.selectedVersion = '';
    const changes: SimpleChanges = {
      selectedVersion: {
        currentValue: '',
        previousValue: '8.0.0',
        firstChange: false,
        isFirstChange: () => false
      },
      productDetail: {
        currentValue: component.productDetail,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    };

    component.ngOnChanges(changes);

    expect(productDetailService.getExternalDocumentForProductByVersion).not.toHaveBeenCalled();
    expect(component.externalDocumentLink).toBe('');
    expect(component.displayExternalDocName).toBe('');
  });

    describe('ngOnInit', () => {
      it('should set displayVersion from selectedVersion', () => {
        const versionDisplay = 'Version 10.0.0';
        spyOn(component, 'extractVersionValue').and.returnValue(TEST_VERSION);
        component.selectedVersion = versionDisplay;

        component.ngOnInit();

        expect(component.extractVersionValue).toHaveBeenCalledWith(
          versionDisplay
        );
        expect(component.displayVersion).toBe(TEST_VERSION);
      });

    });

  it('should extract version value correctly', () => {
    const versionDisplayName = "Version 10.0.0";
    const extractedValue = component.extractVersionValue(versionDisplayName);
    expect(extractedValue).toBe(TEST_VERSION);
  });

  it('should check isProductChanged correct', () => {
    component.productDetail = { id: TEST_ID, newestReleaseVersion: '11.3.0' } as ProductDetail;
    const productChanged: SimpleChange = {
      currentValue: component.productDetail,
      previousValue: undefined,
      firstChange: true,
      isFirstChange: () => true
    };

    const result  = component.isProductChanged(productChanged);
    expect(result).toBe(false);
  });

  it('should check isProductChanged correct if the same value', () => {
    component.productDetail = { id: TEST_ID, newestReleaseVersion: '11.3.0' } as ProductDetail;
    const productChanged: SimpleChange = {
      currentValue: component.productDetail,
      previousValue: component.productDetail.newestReleaseVersion = '12.0.0-m266',
      firstChange: true,
      isFirstChange: () => true
    };

    const result  = component.isProductChanged(productChanged);
    expect(result).toBe(true);
  });

  it('should return false when change is undefined', () => {
    expect(component.isVersionUnchangedOrFirstChange(undefined)).toBeFalse();
  });

  it('should return true when currentValue equals previousValue', () => {
    const change = new SimpleChange('v1', 'v1', false);
    expect(component.isVersionUnchangedOrFirstChange(change)).toBeTrue();
  });

  it('should return true when firstChange is true', () => {
    const change = new SimpleChange('v2', undefined, true);
    expect(component.isVersionUnchangedOrFirstChange(change)).toBeTrue();
  });

  it('should return false when values differ and firstChange is false', () => {
    const change = new SimpleChange('v2', 'v1', false);
    expect(component.isVersionUnchangedOrFirstChange(change)).toBeFalse();
  });

  describe('getShieldsBadgeUrl', () => {
    it('should return empty string if productDetail is undefined', () => {
      component.productDetail = undefined as any;
      expect(component.getShieldsBadgeUrl()).toBe('');
    });

    it('should return empty string if statusBadgeUrl is missing', () => {
      component.productDetail = {
        id: 'repo',
        statusBadgeUrl: ''
      } as ProductDetail;
      expect(component.getShieldsBadgeUrl()).toBe('');
    });

    it('should return correct Shields.io badge URL for valid productDetail', () => {
      component.productDetail = {
        statusBadgeUrl:
          'https://github.com/axonivy-market/keycloak-connector/actions/workflows/ci.yml/badge.svg'
      } as ProductDetail;
      expect(component.getShieldsBadgeUrl()).toBe(
        'https://img.shields.io/github/actions/workflow/status/axonivy-market/keycloak-connector/ci.yml?branch=master'
      );
    });

    it('should navigate to /monitoring with repo name in query params when onBadgeClick is called and productDetail has id', () => {
      component.repoName = 'test-repo';
      component.productDetail = { isFocusedProduct: true } as ProductDetail;
      const navigateSpy = spyOn(component.router, 'navigate');

      component.onBadgeClick();

      expect(navigateSpy).toHaveBeenCalledWith(['/monitoring'], {
        queryParams: { repoSearch: 'test-repo',  activeTab: 'focused' }
      });
    });

    it('should return empty string when statusBadgeUrl is missing', () => {
      component.productDetail = {
        id: 'repo',
        statusBadgeUrl: ''
      } as ProductDetail;
      const result = component.getShieldsBadgeUrl();
      expect(result).toBe('');
    });

    it('should return formatted shields.io badge URL and set repoName', () => {
      component.productDetail = {
        statusBadgeUrl:
          'https://github.com/axonivy-market/keycloak-connector/actions/workflows/ci.yml/badge.svg'
      } as ProductDetail;

      const result = component.getShieldsBadgeUrl();

      expect(component.repoName).toBe('keycloak-connector');
      expect(result).toBe(
        `${SHIELDS_BADGE_BASE_URL}/axonivy-market/keycloak-connector/${SHIELDS_WORKFLOW}?branch=${BRANCH}`
      );
    });

    it('should not navigate when onBadgeClick is called and productDetail is missing repoName', () => {
      component.repoName = '';
      component.productDetail = { isFocusedProduct: false } as ProductDetail;
      const navigateSpy = spyOn(component.router, 'navigate');

      component.onBadgeClick();
      expect(navigateSpy).toHaveBeenCalledWith(['/monitoring']);
    });
  });
});
