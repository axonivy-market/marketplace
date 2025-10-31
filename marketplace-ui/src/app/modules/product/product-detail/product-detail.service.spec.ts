import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProductDetailService } from './product-detail.service';
import { API_URI } from '../../../shared/constants/api.constant';
import { ExternalDocument } from '../../../shared/models/external-document.model';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ForwardingError } from '../../../core/interceptors/api.interceptor';

describe('ProductDetailService', () => {
  let service: ProductDetailService;
  let httpMock: HttpTestingController;

  const mockExternalDoc: ExternalDocument = {
    productId: 'portal',
    version: '10.0.0'
  } as ExternalDocument;

  beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [],
        providers: [
          ProductDetailService,
          provideHttpClient(withInterceptorsFromDi()),
          provideHttpClientTesting()
        ]
      });
    service = TestBed.inject(ProductDetailService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getExternalDocumentForProductByVersion', () => {
    it('should call correct endpoint and return external document', () => {
      const productId = 'p123';
      const version = 'v1';
      const expectedUrl = `${API_URI.EXTERNAL_DOCUMENT}/${productId}/${version}`;

      service.getExternalDocumentForProductByVersion(productId, version).subscribe(res => {
        expect(res).toEqual(mockExternalDoc);
      });

      const req = httpMock.expectOne((r) => r.url === expectedUrl);
      expect(req.request.method).toBe('GET');
      expect(req.request.context.get(ForwardingError)).toBeTrue();

      req.flush(mockExternalDoc);
    });
  });

  describe('getBestMatchVersion', () => {
    it('should call correct endpoint and return version as text', () => {
      const productId = 'p123';
      const version = '1.0.0';
      const isShowDevVersion = true;
      const mockResponse = '1.0.1';
      const expectedUrl = `${API_URI.PRODUCT_DETAILS}/${productId}/${version}/best-match-version?isShowDevVersion=${isShowDevVersion}`;

      service.getBestMatchVersion(productId, version, isShowDevVersion).subscribe(res => {
        expect(res).toBe(mockResponse);
      });

      const req = httpMock.expectOne(expectedUrl);
      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('text');

      req.flush(mockResponse);
    });
  });
});
