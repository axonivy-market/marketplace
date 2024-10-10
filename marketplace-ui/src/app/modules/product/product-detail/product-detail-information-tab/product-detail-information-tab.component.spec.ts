
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductDetailInformationTabComponent } from './product-detail-information-tab.component';
import { of } from 'rxjs';
import { SimpleChanges } from '@angular/core';
import { ProductDetailService } from '../product-detail.service';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ExternalDocument } from '../../../../shared/models/external-document.model';

const TEST_ID = 'portal';
const TEST_VERSION = 'v10.0.0';
const TEST_ACTUAL_VERSION = '10.0.0';
const TEST_ARTIFACT_ID = 'portal-guide';
const TEST_ARTIFACT_NAME = 'Portal Guide';
const TEST_DOC_URL = '/market-cache/portal/portal-guide/10.0.0/doc/index.html';

describe('ProductDetailInformationTabComponent', () => {
  let component: ProductDetailInformationTabComponent;
  let fixture: ComponentFixture<ProductDetailInformationTabComponent>;
  let productDetailService: jasmine.SpyObj<ProductDetailService>;

  beforeEach(async () => {
    const productDetailServiceSpy = jasmine.createSpyObj('ProductDetailService', ['getExteralDocumentForProductByVersion']);

    await TestBed.configureTestingModule({
      imports: [ProductDetailInformationTabComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: ProductDetailService, useValue: productDetailServiceSpy },
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
    const mockResponse = { productId: TEST_ID, version: TEST_VERSION, artifactId: TEST_ARTIFACT_ID, artifactName: TEST_ARTIFACT_NAME, relativeLink: TEST_DOC_URL };
    productDetailService.getExteralDocumentForProductByVersion.and.returnValue(of(mockResponse));

    component.productDetail = { id: TEST_ID, newestReleaseVersion: TEST_VERSION } as ProductDetail;
    component.selectedVersion = TEST_VERSION;
    const changes: SimpleChanges = {
      selectedVersion: {
        currentValue: TEST_VERSION,
        previousValue: 'v8.0.0',
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

    expect(productDetailService.getExteralDocumentForProductByVersion).toHaveBeenCalledWith(TEST_ID, TEST_ACTUAL_VERSION);
    expect(component.externalDocumentLink).toBe(TEST_DOC_URL);
    expect(component.displayExternalDocName).toBe(TEST_ARTIFACT_NAME);
  });

  it('should not set externalDocumentLink if version is invalid', () => {
    component.productDetail = { id: TEST_ID, newestReleaseVersion: '' } as ProductDetail;
    component.selectedVersion = '';
    const changes: SimpleChanges = {
      selectedVersion: {
        currentValue: '',
        previousValue: 'v8.0.0',
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

    expect(productDetailService.getExteralDocumentForProductByVersion).not.toHaveBeenCalled();
    expect(component.externalDocumentLink).toBe('');
    expect(component.displayExternalDocName).toBe('');
  });

  it('should extract version value correctly', () => {
    const versionDisplayName = TEST_VERSION;
    const extractedValue = component.extractVersionValue(versionDisplayName);
    expect(extractedValue).toBe(TEST_ACTUAL_VERSION);
  });
});