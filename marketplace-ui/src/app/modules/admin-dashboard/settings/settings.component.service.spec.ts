import { afterEach, beforeEach, describe, expect, it, type MockedObject, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppSetting, AppSettingsService } from './settings.component.service';
import { AdminAuthService } from '../admin-auth.service';
import { API_URI } from '../../../shared/constants/api.constant';

describe('AppSettingsService', () => {
  let service: AppSettingsService;
  let httpMock: HttpTestingController;
  let adminAuthServiceMock: MockedObject<AdminAuthService>;

  const mockSettings: AppSetting[] = [
    {
      settingKey: 'github.token',
      settingValue: 'secret-value',
      category: 'GITHUB',
      description: 'GitHub API token',
      encrypted: true
    },
    {
      settingKey: 'app.name',
      settingValue: 'marketplace',
      category: 'APPLICATION',
      description: 'Application name',
      encrypted: false
    }
  ];

  beforeEach(() => {
    adminAuthServiceMock = {
      clearToken: vi.fn().mockName('AdminAuthService.clearToken')
    } as MockedObject<AdminAuthService>;

    TestBed.configureTestingModule({
      providers: [
        provideHttpClientTesting(),
        AppSettingsService,
        { provide: AdminAuthService, useValue: adminAuthServiceMock }
      ]
    });

    service = TestBed.inject(AppSettingsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getSettings', () => {
    it('should send GET to APP_SETTINGS', () => {
      service.getSettings().subscribe(response => {
        expect(response).toEqual(mockSettings);
      });

      const req = httpMock.expectOne(r => r.url === API_URI.APP_SETTINGS);
      expect(req.request.method).toBe('GET');
      req.flush(mockSettings);
    });

    it('should not include search param when searchText is empty', () => {
      service.getSettings('').subscribe();

      const req = httpMock.expectOne(r => r.url === API_URI.APP_SETTINGS);
      expect(req.request.params.has('search')).toBe(false);
      req.flush([]);
    });

    it('should not include search param when searchText is whitespace', () => {
      service.getSettings('   ').subscribe();

      const req = httpMock.expectOne(r => r.url === API_URI.APP_SETTINGS);
      expect(req.request.params.has('search')).toBe(false);
      req.flush([]);
    });

    it('should include search param when searchText is provided', () => {
      service.getSettings('github').subscribe();

      const req = httpMock.expectOne(r => r.url === API_URI.APP_SETTINGS);
      expect(req.request.params.get('search')).toBe('github');
      req.flush([]);
    });
  });

  describe('updateSetting', () => {
    it('should send PUT with settingValue in body', () => {
      const setting = mockSettings[0];
      service.updateSetting(setting).subscribe(response => {
        expect(response).toEqual(setting);
      });

      const expectedUrl = `${API_URI.APP_SETTINGS}/${encodeURIComponent(setting.settingKey)}`;
      const req = httpMock.expectOne(r => r.url === expectedUrl);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ settingValue: setting.settingValue });
      req.flush(setting);
    });

    it('should URL-encode special characters in settingKey', () => {
      const setting: AppSetting = { ...mockSettings[0], settingKey: 'github.api/token' };
      service.updateSetting(setting).subscribe();

      const expectedUrl = `${API_URI.APP_SETTINGS}/${encodeURIComponent(setting.settingKey)}`;
      const req = httpMock.expectOne(r => r.url === expectedUrl);
      expect(req.request.method).toBe('PUT');
      req.flush(setting);
    });
  });
});
