import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { LogStreamService } from './log-stream.service';
import { RuntimeConfigService } from '../../configs/runtime-config.service';
import { API_INTERNAL_URL, API_PUBLIC_URL, API_URI } from '../../../shared/constants/api.constant';
import { RUNTIME_CONFIG_KEYS } from '../../models/runtime-config';

import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { HttpHeaders } from '@angular/common/http';

describe('LogStreamService', () => {
  let service: LogStreamService;
  let mockRuntimeConfig: jasmine.SpyObj<RuntimeConfigService>;
  let mockAdminAuthService: jasmine.SpyObj<AdminAuthService>;
  const mockBaseUrl = 'http://localhost:8080';
  let fetchSpy: jasmine.Spy;

  beforeEach(() => {
    mockRuntimeConfig = jasmine.createSpyObj('RuntimeConfigService', ['get']);
    mockRuntimeConfig.get.and.returnValue(mockBaseUrl);

    mockAdminAuthService = jasmine.createSpyObj('AdminAuthService', [
      'getAuthHeaders'
    ]);
    mockAdminAuthService.getAuthHeaders.and.returnValue(new HttpHeaders());

    fetchSpy = spyOn(window, 'fetch').and.returnValue(new Promise(() => {}));

    TestBed.configureTestingModule({
      providers: [
        LogStreamService,
        { provide: RuntimeConfigService, useValue: mockRuntimeConfig },
        { provide: AdminAuthService, useValue: mockAdminAuthService },
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
    it('should establish connection with public URL in browser', () => {
      service.connect();
      expect(fetchSpy).toHaveBeenCalled();
      const args = fetchSpy.calls.mostRecent().args;
      const url = args[0] as string;
      expect(url).toContain('http://public:8080');
      expect(url).toContain('/logs/stream');
      expect(service.isConnected()).toBe(true);
    });

    it('should use runtime config URL if public URL is missing in browser', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          LogStreamService,
          { provide: RuntimeConfigService, useValue: mockRuntimeConfig },
          { provide: AdminAuthService, useValue: mockAdminAuthService },
          { provide: PLATFORM_ID, useValue: 'browser' },
          { provide: API_INTERNAL_URL, useValue: 'http://internal:8080' }
        ]
      });
      const browserService = TestBed.inject(LogStreamService);
      browserService.connect();
      expect(mockRuntimeConfig.get).toHaveBeenCalledWith(
        RUNTIME_CONFIG_KEYS.MARKET_API_URL
      );
      expect(fetchSpy).toHaveBeenCalled();
      const args = fetchSpy.calls.mostRecent().args;
      const url = args[0] as string;
      expect(url).toContain(mockBaseUrl);
    });

    it('should not connect if on server platform', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          LogStreamService,
          { provide: RuntimeConfigService, useValue: mockRuntimeConfig },
          { provide: AdminAuthService, useValue: mockAdminAuthService },
          { provide: PLATFORM_ID, useValue: 'server' }
        ]
      });
      const serverService = TestBed.inject(LogStreamService);
      serverService.connect();
      expect(fetchSpy).not.toHaveBeenCalled();
      expect(serverService.isConnected()).toBeFalse();
    });

    it('should not connect if already connected', () => {
      service.connect();
      service.connect();
      expect(fetchSpy).toHaveBeenCalledTimes(1);
    });
  });

  describe('disconnection and state', () => {
    it('should disconnect and abort controller', () => {
      service.connect();
      expect(service.isConnected()).toBe(true);
      service.disconnect();
      expect(service.isConnected()).toBe(false);
    });

    it('should clear all logs', () => {
      (service as any)._logs.set(['log1', 'log2', 'log3']);
      service.clear();
      expect(service.logs()).toEqual([]);
    });

    it('should return correct connection status state', () => {
      expect(service.isConnected()).toBe(false);
      service.connect();
      expect(service.isConnected()).toBe(true);
      service.disconnect();
      expect(service.isConnected()).toBe(false);
    });
  });
});
