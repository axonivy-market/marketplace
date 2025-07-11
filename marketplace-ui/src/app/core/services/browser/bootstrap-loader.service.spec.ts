import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { BootstrapLoaderService } from './bootstrap-loader.service';

describe('BootstrapLoaderService', () => {
  let service: BootstrapLoaderService;

  describe('in browser platform', () => {
    let importSpy: jasmine.Spy;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          BootstrapLoaderService,
          { provide: PLATFORM_ID, useValue: 'browser' }
        ]
      });

      service = TestBed.inject(BootstrapLoaderService);
    });

    it('should call loadNgbModule() in browser', async () => {
      const spy = spyOn(service as any, 'loadNgbModule').and.resolveTo({});
      await service.init();
      expect(spy).toHaveBeenCalled();
    });
  });

  describe('in server platform', () => {
    let importSpy: jasmine.Spy;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          BootstrapLoaderService,
          { provide: PLATFORM_ID, useValue: 'server' }
        ]
      });

      service = TestBed.inject(BootstrapLoaderService);
    });

    fit('should NOT call loadNgbModule() on server', async () => {
      const spy = spyOn(service as any, 'loadNgbModule');
      await service.init();
      expect(spy).not.toHaveBeenCalled();
    });
  });
});
