import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { LogStreamService } from './log-stream.service';
import { RuntimeConfigService } from '../../configs/runtime-config.service';
import { API_INTERNAL_URL, API_PUBLIC_URL, API_URI } from '../../../shared/constants/api.constant';
import { RUNTIME_CONFIG_KEYS } from '../../models/runtime-config';

describe('LogStreamService', () => {
  let service: LogStreamService;
  let mockRuntimeConfig: jasmine.SpyObj<RuntimeConfigService>;
  const mockBaseUrl = 'http://localhost:8080';

  beforeEach(() => {
    mockRuntimeConfig = jasmine.createSpyObj('RuntimeConfigService', ['get']);
    mockRuntimeConfig.get.and.returnValue(mockBaseUrl);

    TestBed.configureTestingModule({
      providers: [
        LogStreamService,
        { provide: RuntimeConfigService, useValue: mockRuntimeConfig },
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: API_INTERNAL_URL, useValue: 'http://internal:8080' },
        { provide: API_PUBLIC_URL, useValue: 'http://public:8080' }
      ]
    });

    service = TestBed.inject(LogStreamService);
  });

  afterEach(() => {
    service.disconnect();
  });

  it('should create the service', () => {
    expect(service).toBeTruthy();
  });

  describe('connect', () => {
    it('should establish EventSource with public URL in browser', () => {
      const publicUrl = 'http://public:8080';
      const eventSourceSpy = spyOn(window, 'EventSource').and.returnValue({
        onmessage: null,
        onerror: null,
        close: jasmine.createSpy('close')
      } as any);

      service.connect();

      expect(eventSourceSpy).toHaveBeenCalledWith(
        `${publicUrl}/${API_URI.LOGS}/stream`
      );
      expect(service.isConnected()).toBe(true);
    });

    it('should use runtime config URL if public URL is missing in browser', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          LogStreamService,
          { provide: RuntimeConfigService, useValue: mockRuntimeConfig },
          { provide: PLATFORM_ID, useValue: 'browser' }
          // API_PUBLIC_URL is omitted
        ]
      });
      const browserService = TestBed.inject(LogStreamService);
      const eventSourceSpy = spyOn(window, 'EventSource').and.returnValue(
        {} as any
      );

      browserService.connect();

      expect(mockRuntimeConfig.get).toHaveBeenCalledWith(
        RUNTIME_CONFIG_KEYS.MARKET_API_URL
      );
      expect(eventSourceSpy).toHaveBeenCalledWith(
        `${mockBaseUrl}/${API_URI.LOGS}/stream`
      );
    });

    it('should not connect if on server platform', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          LogStreamService,
          { provide: RuntimeConfigService, useValue: mockRuntimeConfig },
          { provide: PLATFORM_ID, useValue: 'server' }
        ]
      });
      const serverService = TestBed.inject(LogStreamService);
      spyOn(window, 'EventSource');

      serverService.connect();

      expect(window.EventSource).not.toHaveBeenCalled();
      expect(serverService.isConnected()).toBeFalse();
    });

    it('should not connect if already connected', () => {
      spyOn(window, 'EventSource').and.returnValue({
        close: jasmine.createSpy('close')
      } as any);
      service.connect();
      service.connect(); // Second call

      expect(window.EventSource).toHaveBeenCalledTimes(1);
    });

    it('should handle onmessage and append logs', () => {
      const mockES = {
        onmessage: null,
        close: jasmine.createSpy('close')
      } as any;
      spyOn(window, 'EventSource').and.returnValue(mockES);
      service.connect();

      mockES.onmessage({ data: 'new log line' });

      expect(service.logs()).toEqual(['new log line']);
    });

    it('should limit logs to 2000 lines (overflow logic)', () => {
      const mockES = {
        onmessage: null,
        close: jasmine.createSpy('close')
      } as any;
      spyOn(window, 'EventSource').and.returnValue(mockES);
      service.connect();

      // Fill logs to max (2000 lines)
      const initialLogs = Array.from({ length: 2000 }, (_, i) => `line ${i}`);
      service['_logs'].set(initialLogs);

      mockES.onmessage({ data: 'overflow line' });

      const logs = service.logs();
      expect(logs.length).toBe(2000);
      expect(logs[1999]).toBe('overflow line');
      expect(logs[0]).toBe('line 1');
    });

    it('should disconnect on EventSource error', () => {
      const mockES = {
        onmessage: null,
        onerror: null,
        close: jasmine.createSpy('close')
      } as any;
      spyOn(window, 'EventSource').and.returnValue(mockES);

      service.connect();
      mockES.onerror(new Event('error'));

      expect(mockES.close).toHaveBeenCalled();
      expect(service.isConnected()).toBeFalse();
    });
  });

  describe('disconnection and state', () => {
    it('should disconnect and close EventSource', () => {
      const mockEventSource = {
        onmessage: null,
        onerror: null,
        close: jasmine.createSpy('close')
      } as any;

      spyOn(window, 'EventSource').and.returnValue(mockEventSource);
      service.connect();
      service.disconnect();

      expect(mockEventSource.close).toHaveBeenCalled();
      expect(service.isConnected()).toBe(false);
    });

    it('should clear all logs', () => {
      service['_logs'].set(['log1', 'log2', 'log3']);
      service.clear();
      expect(service.logs()).toEqual([]);
    });

    it('should return correct connection status state', () => {
      expect(service.isConnected()).toBe(false);
      spyOn(window, 'EventSource').and.returnValue({
        close: jasmine.createSpy('close')
      } as any);
      service.connect();
      expect(service.isConnected()).toBe(true);
      service.disconnect();
      expect(service.isConnected()).toBe(false);
    });
  });
});
