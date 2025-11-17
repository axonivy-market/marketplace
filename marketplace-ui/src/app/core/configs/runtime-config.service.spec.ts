import { TestBed } from '@angular/core/testing';
import { TransferState, PLATFORM_ID } from '@angular/core';
import { RuntimeConfigService } from './runtime-config.service';
import { RUNTIME_CONFIG_KEY, RuntimeConfig } from '../models/runtime-config';

describe('RuntimeConfigService', () => {
  let service: RuntimeConfigService;
  let transferState: TransferState;

  const mockConfig: RuntimeConfig = {
    apiUrl: '/test-api',
    githubOAuthAppClientId: 'test-client-id',
    githubOAuthCallback: '/test/callback',
    githubApiUrl: 'https://test.api.github.com',
    dayInMiliseconds: 86400000,
    matomoSiteId: 999,
    matomoTrackerUrl: '//test.tracker.com'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RuntimeConfigService,
        TransferState,
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    });
    service = TestBed.inject(RuntimeConfigService);
    transferState = TestBed.inject(TransferState);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getConfig', () => {
    it('should return configuration from TransferState when available', () => {
      transferState.set(RUNTIME_CONFIG_KEY, mockConfig);
      
      const config = service.getConfig();
      
      expect(config).toEqual(mockConfig);
    });

    it('should cache configuration after first call', () => {
      transferState.set(RUNTIME_CONFIG_KEY, mockConfig);
      
      const config1 = service.getConfig();
      const config2 = service.getConfig();
      
      expect(config1).toBe(config2); // Same reference
    });

    it('should fallback to environment when TransferState is empty', () => {
      const config = service.getConfig();
      
      expect(config).toBeDefined();
      expect(config.apiUrl).toBeDefined();
      expect(config.githubOAuthAppClientId).toBeDefined();
    });
  });

  describe('setConfig', () => {
    it('should set configuration and store in TransferState', () => {
      service.setConfig(mockConfig);
      
      const storedConfig = transferState.get(RUNTIME_CONFIG_KEY, null);
      expect(storedConfig).toEqual(mockConfig);
    });

    it('should update cached configuration', () => {
      service.setConfig(mockConfig);
      
      const config = service.getConfig();
      expect(config).toEqual(mockConfig);
    });
  });

  describe('get', () => {
    it('should return specific configuration value', () => {
      transferState.set(RUNTIME_CONFIG_KEY, mockConfig);
      
      expect(service.get('apiUrl')).toBe(mockConfig.apiUrl);
      expect(service.get('matomoSiteId')).toBe(mockConfig.matomoSiteId);
      expect(service.get('githubOAuthAppClientId')).toBe(mockConfig.githubOAuthAppClientId);
    });

    it('should be type-safe', () => {
      transferState.set(RUNTIME_CONFIG_KEY, mockConfig);
      
      const apiUrl: string = service.get('apiUrl');
      const matomoSiteId: number = service.get('matomoSiteId');
      
      expect(typeof apiUrl).toBe('string');
      expect(typeof matomoSiteId).toBe('number');
    });
  });

  describe('constructor with provided config', () => {
    it('should accept config via DI on server', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          RuntimeConfigService,
          TransferState,
          { provide: PLATFORM_ID, useValue: 'server' },
          { provide: RUNTIME_CONFIG_KEY, useValue: mockConfig }
        ]
      });
      
      const serverService = TestBed.inject(RuntimeConfigService);
      const serverTransferState = TestBed.inject(TransferState);
      
      // Config should be stored in TransferState
      const storedConfig = serverTransferState.get(RUNTIME_CONFIG_KEY, null);
      expect(storedConfig).toEqual(mockConfig);
    });
  });

  describe('SSR behavior', () => {
    it('should store config in TransferState on server platform', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          RuntimeConfigService,
          TransferState,
          { provide: PLATFORM_ID, useValue: 'server' },
          { provide: RUNTIME_CONFIG_KEY, useValue: mockConfig }
        ]
      });
      
      const serverTransferState = TestBed.inject(TransferState);
      const serverService = TestBed.inject(RuntimeConfigService);
      
      // Verify the config is set correctly
      expect(serverService.getConfig()).toEqual(mockConfig);
      
      // Verify it was stored in TransferState
      const transferredConfig = serverTransferState.get(RUNTIME_CONFIG_KEY, null);
      expect(transferredConfig).toEqual(mockConfig);
    });

    it('should read from TransferState on browser platform', () => {
      // Setup TransferState as if it came from SSR
      transferState.set(RUNTIME_CONFIG_KEY, mockConfig);
      
      const config = service.getConfig();
      
      expect(config).toEqual(mockConfig);
    });
  });
});
