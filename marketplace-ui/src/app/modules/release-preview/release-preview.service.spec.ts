import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReleasePreviewService } from './release-preview.service';
import { environment } from '../../../environments/environment';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
import { MOCK_RELEASE_PREVIEW_DATA } from '../../shared/mocks/mock-data';

describe('SecurityMonitorService', () => {
    let service: ReleasePreviewService;
    let httpMock: HttpTestingController;

    const mockApiUrl = environment.apiUrl + '/api/release-preview';

    beforeEach(() => {
        TestBed.configureTestingModule({
          providers: [
            ReleasePreviewService,
            provideHttpClient(withInterceptorsFromDi()),
            provideHttpClientTesting()
          ]
        });
        service = TestBed.inject(ReleasePreviewService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should call API and return Readme data', () => {
        const mockFile = new File(['content'], 'test.zip', {
          type: 'application/zip'
        });
        const mockResponse: ReleasePreviewData = MOCK_RELEASE_PREVIEW_DATA

        service.extractZipDetails(mockFile).subscribe(data => {
          expect(data).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(mockApiUrl);
        expect(req.request.method).toBe('POST');

        req.flush(mockResponse);
    });
});