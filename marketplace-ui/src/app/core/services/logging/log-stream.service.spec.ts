import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { LogStreamService } from './log-stream.service';
import { RuntimeConfigService } from '../../configs/runtime-config.service';
import { API_INTERNAL_URL, API_PUBLIC_URL, API_URI } from '../../../shared/constants/api.constant';

describe('LogStreamService', () => {
  let service: LogStreamService;
  let mockRuntimeConfig: jasmine.SpyObj<RuntimeConfigService>;

  beforeEach(() => {
    mockRuntimeConfig = jasmine.createSpyObj('RuntimeConfigService', ['get']);
    mockRuntimeConfig.get.and.returnValue('http://localhost:8080');

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

  it('should connect and establish EventSource', () => {
    spyOn(window, 'EventSource').and.returnValue({
      onmessage: null,
      onerror: null,
      close: jasmine.createSpy('close')
    } as any);

    service.connect();

    expect(window.EventSource).toHaveBeenCalled();
    expect(service.isConnected()).toBe(true);
  });

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
    service.connect();
    // Simulate adding logs
    service['_logs'].set(['log1', 'log2', 'log3']);

    service.clear();

    expect(service.logs()).toEqual([]);
  });

  it('should return connection status', () => {
    expect(service.isConnected()).toBe(false);

    spyOn(window, 'EventSource').and.returnValue({
      onmessage: null,
      onerror: null,
      close: jasmine.createSpy('close')
    } as any);

    service.connect();

    expect(service.isConnected()).toBe(true);

    service.disconnect();

    expect(service.isConnected()).toBe(false);
  });
});
