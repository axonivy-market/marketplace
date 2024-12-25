import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReleasePreviewService } from './release-preview.service';
import { environment } from '../../../environments/environment';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';

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
        const mockResponse: ReleasePreviewData = {
          description: {
            English: 'This is a description in English.',
            Spanish: 'Esta es una descripción en español.',
            French: 'Ceci est une description en français.'
          },
          setup: {
            English: 'To set up the application, follow these steps...',
            Spanish: 'Para configurar la aplicación, siga estos pasos...',
            French: "Pour configurer l'application, suivez ces étapes..."
          },
          demo: {
            English: 'To demo the app, use the following commands...',
            Spanish:
              'Para mostrar la aplicación, use los siguientes comandos...',
            French:
              "Pour démontrer l'application, utilisez les commandes suivantes..."
          }
        };

        service.extractZipDetails(mockFile).subscribe(data => {
          expect(data).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(mockApiUrl);
        expect(req.request.method).toBe('POST');

        req.flush(mockResponse);
    });
});