import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LogService } from './log.service';
import { API_URI } from '../../shared/constants/api.constant';
import { LogFileModel } from '../../shared/models/apis/log-file-response.model';
import { HttpResponse } from '@angular/common/http';

describe('LogService', () => {
  let service: LogService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LogService]
    });
    service = TestBed.inject(LogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getLogFiles', () => {
    it('should fetch log files with date param', () => {
      const mockLogs: LogFileModel[] = [
        { fileName: 'test.log', size: 100, date: '2026-02-26' }
      ];
      const testDate = '2026-02-26';

      service.getLogFiles(testDate).subscribe((logs) => {
        expect(logs).toEqual(mockLogs);
      });

      const req = httpMock.expectOne((request) => 
        request.url === API_URI.LOGS && request.params.get('date') === testDate
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockLogs);
    });

    it('should fetch log files without date param', () => {
      const mockLogs: LogFileModel[] = [];

      service.getLogFiles().subscribe((logs) => {
        expect(logs).toEqual(mockLogs);
      });

      const req = httpMock.expectOne((request) => 
        request.url === API_URI.LOGS && request.params.get('date') === ''
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockLogs);
    });
  });

  describe('getLogFileContent', () => {
    it('should fetch log file content and trigger download', () => {
      const fileName = 'test.log';
      const mockBlob = new Blob(['test content'], { type: 'text/plain' });
      const triggerDownloadSpy = spyOn(service, 'triggerDownload');

      service.getLogFileContent(fileName);

      const req = httpMock.expectOne((request) => 
        request.url === `${API_URI.LOGS}/download` && request.params.get('fileName') === fileName
      );
      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('blob');
      
      req.event(new HttpResponse({ body: mockBlob, status: 200 }));
      
      expect(triggerDownloadSpy).toHaveBeenCalledWith(mockBlob, fileName);
    });

    it('should not trigger download if response body is null', () => {
        const fileName = 'test.log';
        const triggerDownloadSpy = spyOn(service, 'triggerDownload');
  
        service.getLogFileContent(fileName);
  
        const req = httpMock.expectOne((request) => 
          request.url === `${API_URI.LOGS}/download` && request.params.get('fileName') === fileName
        );
        
        req.event(new HttpResponse({ body: null, status: 200 }));
        
        expect(triggerDownloadSpy).not.toHaveBeenCalled();
      });
  });

  describe('triggerDownload', () => {
    it('should create an anchor element and trigger click', () => {
      const mockBlob = new Blob(['test'], { type: 'text/plain' });
      const fileName = 'download.log';
      const mockUrl = 'blob:test-url';
      
      const anchorSpy = jasmine.createSpyObj('HTMLAnchorElement', ['click']);
      spyOn(document, 'createElement').and.returnValue(anchorSpy);
      spyOn(URL, 'createObjectURL').and.returnValue(mockUrl);
      spyOn(URL, 'revokeObjectURL');

      service.triggerDownload(mockBlob, fileName);

      expect(URL.createObjectURL).toHaveBeenCalledWith(mockBlob);
      expect(document.createElement).toHaveBeenCalledWith('a');
      expect(anchorSpy.href).toBe(mockUrl);
      expect(anchorSpy.download).toBe(fileName);
      expect(anchorSpy.click).toHaveBeenCalled();
      expect(URL.revokeObjectURL).toHaveBeenCalledWith(mockUrl);
    });
  });
});
