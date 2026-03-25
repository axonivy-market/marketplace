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

    fetchSpy = spyOn(window, 'fetch').and.returnValue(new Promise(() => { }));

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

    it('should include auth headers in connection', () => {
      const dummyHeaders = new HttpHeaders().set(
        'Authorization',
        'Bearer token'
      );
      mockAdminAuthService.getAuthHeaders.and.returnValue(dummyHeaders);

      service.connect();

      expect(fetchSpy).toHaveBeenCalled();
      const args = fetchSpy.calls.mostRecent().args;
      const options = args[1];
      expect(options.headers).toEqual(
        jasmine.objectContaining({ Authorization: 'Bearer token' })
      );
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

  describe('task-key stream methods', () => {
    const TASK_KEY = 'syncProducts';

    beforeEach(() => {
      Object.defineProperty(mockAdminAuthService, 'token', {
        get: () => 'mock-token',
        configurable: true
      });
    });

    afterEach(() => {
      service.disconnectTask(TASK_KEY);
      service.disconnectTask('syncGithubMonitor');
    });

    describe('getLogs', () => {
      it('should return empty array when taskKey is undefined', () => {
        expect(service.getLogs(undefined)).toEqual([]);
      });

      it('should return empty array when taskKey is empty string', () => {
        expect(service.getLogs('')).toEqual([]);
      });

      it('should return empty array when taskKey has no logs', () => {
        expect(service.getLogs(TASK_KEY)).toEqual([]);
      });

      it('should return logs after added', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1', 'line 2']);
          return next;
        });

        expect(service.getLogs(TASK_KEY)).toEqual(['line 1', 'line 2']);
      });

      it('should return empty for unknown taskKey', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1']);
          return next;
        });

        expect(service.getLogs('unknownKey')).toEqual([]);
      });
    });

    describe('getLogsSignal', () => {
      it('should return empty array when taskKey signal returns undefined', () => {
        const sig = service.getLogsSignal(() => undefined);
        TestBed.runInInjectionContext(() => {
          expect(sig()).toEqual([]);
        });
      });

      it('should return empty array when taskKey signal returns empty string', () => {
        const sig = service.getLogsSignal(() => '');
        TestBed.runInInjectionContext(() => {
          expect(sig()).toEqual([]);
        });
      });

      it('should return logs reactively when taskLogs updates', () => {
        const sig = service.getLogsSignal(() => TASK_KEY);

        TestBed.runInInjectionContext(() => {
          expect(sig()).toEqual([]);

          service['taskLogs'].update(map => {
            const next = new Map(map);
            next.set(TASK_KEY, ['line 1', 'line 2']);
            return next;
          });

          expect(sig()).toEqual(['line 1', 'line 2']);
        });
      });

      it('should return empty array for different taskKey than stored', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1']);
          return next;
        });

        const sig = service.getLogsSignal(() => 'syncGithubMonitor');
        TestBed.runInInjectionContext(() => {
          expect(sig()).toEqual([]);
        });
      });
    });

    describe('hasLogs', () => {
      it('should return false when no logs', () => {
        expect(service.hasLogs(TASK_KEY)).toBeFalse();
      });

      it('should return true when has logs', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1']);
          return next;
        });

        expect(service.hasLogs(TASK_KEY)).toBeTrue();
      });

      it('should return false after resetTask', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1']);
          return next;
        });

        service.resetTask(TASK_KEY);

        expect(service.hasLogs(TASK_KEY)).toBeFalse();
      });
    });

    describe('connectTask', () => {
      it('should not connect when token is missing', () => {
        Object.defineProperty(mockAdminAuthService, 'token', {
          get: () => null,
          configurable: true
        });

        service.connectTask(TASK_KEY);

        expect(fetchSpy).not.toHaveBeenCalled();
      });

      it('should call fetch with correct url containing taskKey', () => {
        service.connectTask(TASK_KEY);

        expect(fetchSpy).toHaveBeenCalled();
        const url = fetchSpy.calls.mostRecent().args[0] as string;
        expect(url).toContain(`stream/${TASK_KEY}`);
      });

      it('should use public URL in browser', () => {
        service.connectTask(TASK_KEY);

        const url = fetchSpy.calls.mostRecent().args[0] as string;
        expect(url).toContain('http://public:8080');
      });

      it('should set controller for taskKey after connect', () => {
        service.connectTask(TASK_KEY);

        expect(service['controllers'].has(TASK_KEY)).toBeTrue();
      });

      it('should not reconnect if already connected for same taskKey', () => {
        service.connectTask(TASK_KEY);
        service.connectTask(TASK_KEY);

        expect(fetchSpy).toHaveBeenCalledTimes(1);
      });

      it('should connect different taskKeys independently', () => {
        service.connectTask(TASK_KEY);
        service.connectTask('syncGithubMonitor');

        expect(fetchSpy).toHaveBeenCalledTimes(2);
      });

      it('should include Authorization header', () => {
        service.connectTask(TASK_KEY);

        const options = fetchSpy.calls.mostRecent().args[1];
        expect(options.headers).toEqual(
          jasmine.objectContaining({ Authorization: 'Bearer mock-token' })
        );
      });

      it('should handle onmessage and update logs', async () => {
        spyOn<any>(service, '_fetchEventSource').and.callFake(
          (url: string, options: any) => {
            options.onmessage({
              data: 'log line 1'
            });
            return Promise.resolve();
          }
        );

        service.connectTask(TASK_KEY);

        await Promise.resolve();

        expect(service.getLogs(TASK_KEY)).toEqual(["log line 1"]);
      });

      it('should ignore empty event data', async () => {
        spyOn<any>(service, '_fetchEventSource').and.callFake((url: string, options: any) => {
          options.onmessage({ data: null });
          return Promise.resolve();
        });

        service.connectTask(TASK_KEY);

        expect(service.getLogs(TASK_KEY)).toEqual([]);
      });

      it('should handle onerror and disconnect task', async () => {
        spyOn<any>(service, '_fetchEventSource').and.callFake((url: string, options: any) => {
          options.onerror(new Error('fail'));
          return Promise.reject();
        });

        service.connectTask(TASK_KEY);

        expect(service['controllers'].has(TASK_KEY)).toBeFalse();
      });

      it('should handle onopen error response', async () => {
        spyOn<any>(service, '_fetchEventSource').and.callFake((url: string, options: any) => {
          options.onopen({ ok: false, status: 500 });
          return Promise.resolve();
        });

        service.connectTask(TASK_KEY);

        expect(service['controllers'].has(TASK_KEY)).toBeFalse();
      });
    });

    describe('disconnectTask', () => {
      it('should remove controller after disconnect', () => {
        service.connectTask(TASK_KEY);
        expect(service['controllers'].has(TASK_KEY)).toBeTrue();

        service.disconnectTask(TASK_KEY);

        expect(service['controllers'].has(TASK_KEY)).toBeFalse();
      });

      it('should not throw when disconnecting non-connected taskKey', () => {
        expect(() => service.disconnectTask('nonExistent')).not.toThrow();
      });

      it('should only disconnect specified taskKey', () => {
        service.connectTask(TASK_KEY);
        service.connectTask('syncGithubMonitor');

        service.disconnectTask(TASK_KEY);

        expect(service['controllers'].has(TASK_KEY)).toBeFalse();
        expect(service['controllers'].has('syncGithubMonitor')).toBeTrue();
      });
    });

    describe('resetTask', () => {
      it('should clear logs for taskKey', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1', 'line 2']);
          return next;
        });

        service.resetTask(TASK_KEY);

        expect(service.getLogs(TASK_KEY)).toEqual([]);
      });

      it('should disconnect when resetting', () => {
        service.connectTask(TASK_KEY);
        service.resetTask(TASK_KEY);

        expect(service['controllers'].has(TASK_KEY)).toBeFalse();
      });

      it('should not affect other taskKeys', () => {
        service['taskLogs'].update(map => {
          const next = new Map(map);
          next.set(TASK_KEY, ['line 1']);
          next.set('syncGithubMonitor', ['line 2']);
          return next;
        });

        service.resetTask(TASK_KEY);

        expect(service.getLogs(TASK_KEY)).toEqual([]);
        expect(service.getLogs('syncGithubMonitor')).toEqual(['line 2']);
      });

      it('should not throw when resetting non-existent taskKey', () => {
        expect(() => service.resetTask('nonExistent')).not.toThrow();
      });
    });
  });
});
