import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { AdminSettingsComponent } from './settings.component';
import { AppSetting, AppSettingsService } from './settings.component.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { PageTitleService } from '../../../shared/services/page-title.service';

const MOCK_SETTINGS: AppSetting[] = [
  {
    settingKey: 'github.token',
    settingValue: 'secret',
    category: 'GITHUB',
    description: 'GitHub API token',
    encrypted: true
  },
  {
    settingKey: 'schedule.interval',
    settingValue: '30',
    category: 'SCHEDULING',
    description: 'Scheduling interval in minutes',
    encrypted: false
  },
  {
    settingKey: 'app.name',
    settingValue: 'marketplace',
    category: 'APPLICATION',
    description: 'Application display name',
    encrypted: false
  }
];

describe('AdminSettingsComponent', () => {
  let component: AdminSettingsComponent;
  let fixture: ComponentFixture<AdminSettingsComponent>;
  let appSettingsServiceMock: MockedObject<AppSettingsService>;
  let pageTitleServiceMock: MockedObject<PageTitleService>;

  beforeEach(async () => {
    appSettingsServiceMock = {
      getSettings: vi.fn().mockName('AppSettingsService.getSettings'),
      updateSetting: vi.fn().mockName('AppSettingsService.updateSetting')
    } as any;
    appSettingsServiceMock.getSettings.mockReturnValue(of(MOCK_SETTINGS));
    appSettingsServiceMock.updateSetting.mockReturnValue(of(MOCK_SETTINGS[0]));

    pageTitleServiceMock = {
      setTitleOnLangChange: vi
        .fn()
        .mockName('PageTitleService.setTitleOnLangChange')
    } as any;

    const languageServiceMock = {
      selectedLanguage: vi.fn().mockName('LanguageService.selectedLanguage')
    };
    languageServiceMock.selectedLanguage.mockReturnValue('en');

    await TestBed.configureTestingModule({
      imports: [AdminSettingsComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AppSettingsService, useValue: appSettingsServiceMock },
        { provide: PageTitleService, useValue: pageTitleServiceMock },
        { provide: LanguageService, useValue: languageServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should set page title', () => {
      expect(pageTitleServiceMock.setTitleOnLangChange).toHaveBeenCalledWith(
        'common.admin.settings.title'
      );
    });

    it('should load and populate settings', () => {
      expect(appSettingsServiceMock.getSettings).toHaveBeenCalled();
      expect(component['filteredSettings']).toEqual(MOCK_SETTINGS);
    });
  });

  describe('pagedSettings', () => {
    it('should return first page slice', () => {
      component['pageSize'] = 2;
      component['page'] = 1;
      expect(component['pagedSettings']).toEqual(MOCK_SETTINGS.slice(0, 2));
    });

    it('should return second page slice', () => {
      component['pageSize'] = 2;
      component['page'] = 2;
      expect(component['pagedSettings']).toEqual([MOCK_SETTINGS[2]]);
    });

    it('should return all settings when pageSize is -1', () => {
      component['pageSize'] = -1;
      expect(component['pagedSettings']).toEqual(MOCK_SETTINGS);
    });
  });

  describe('totalElements', () => {
    it('should return count of filteredSettings', () => {
      expect(component['totalElements']).toBe(MOCK_SETTINGS.length);
    });
  });

  describe('onPageChange', () => {
    it('should update current page', () => {
      component['onPageChange'](3);
      expect(component['page']).toBe(3);
    });
  });

  describe('onSearchChanged', () => {
    it('should update searchText and reset page to 1', () => {
      component['page'] = 3;
      component['onSearchChanged']('github');
      expect(component['searchText']).toBe('github');
      expect(component['page']).toBe(1);
    });

    it('should filter by settingKey after debounce', () => {
      vi.useFakeTimers();
      component['onSearchChanged']('github');
      vi.advanceTimersByTime(300);
      expect(component['filteredSettings'].length).toBe(1);
      expect(component['filteredSettings'][0].settingKey).toBe('github.token');
    });

    it('should filter by category after debounce', () => {
      vi.useFakeTimers();
      component['onSearchChanged']('SCHEDULING');
      vi.advanceTimersByTime(300);
      expect(component['filteredSettings'].length).toBe(1);
      expect(component['filteredSettings'][0].settingKey).toBe('schedule.interval');
    });

    it('should filter by description after debounce', () => {
      vi.useFakeTimers();
      component['onSearchChanged']('Application display');
      vi.advanceTimersByTime(300);
      expect(component['filteredSettings'].length).toBe(1);
      expect(component['filteredSettings'][0].settingKey).toBe('app.name');
    });

    it('should restore all settings when search is cleared', () => {
      vi.useFakeTimers();
      component['onSearchChanged']('github');
      vi.advanceTimersByTime(300);
      component['onSearchChanged']('');
      vi.advanceTimersByTime(300);
      expect(component['filteredSettings']).toEqual(MOCK_SETTINGS);
    });
  });

  describe('onClearSearch', () => {
    it('should clear searchText, reset page and restore all settings', () => {
      component['searchText'] = 'github';
      component['page'] = 3;
      component['onClearSearch']();
      expect(component['searchText']).toBe('');
      expect(component['page']).toBe(1);
      expect(component['filteredSettings']).toEqual(MOCK_SETTINGS);
    });
  });

  describe('save', () => {
    it('should call updateSetting with the given setting', () => {
      component['save'](MOCK_SETTINGS[0]);
      expect(appSettingsServiceMock.updateSetting).toHaveBeenCalledWith(
        MOCK_SETTINGS[0]
      );
    });
  });

  describe('sortBy', () => {
    it('should set new column with ascending direction', () => {
      component['sortBy']('settingKey');
      expect(component['sortColumn']).toBe('settingKey');
      expect(component['sortDirection']).toBe('asc');
    });

    it('should toggle to descending on same column', () => {
      component['sortBy']('category');
      expect(component['sortDirection']).toBe('desc');
    });

    it('should toggle back to ascending on second click', () => {
      component['sortBy']('category');
      component['sortBy']('category');
      expect(component['sortDirection']).toBe('asc');
    });

    it('should sort filteredSettings ascending by category', () => {
      component['sortColumn'] = 'settingKey';
      component['sortDirection'] = 'desc';
      component['sortBy']('category');
      const categories = component['filteredSettings'].map(s => s.category);
      expect(categories).toEqual([...categories].sort((a, b) => a.localeCompare(b)));
    });

    it('should sort filteredSettings descending when toggled', () => {
      component['sortBy']('category');
      const categories = component['filteredSettings'].map(s => s.category);
      expect(categories).toEqual([...categories].sort((a, b) => b.localeCompare(a)));
    });
  });

  describe('getSortIcon', () => {
    it('should return ti-arrows-sort for inactive column', () => {
      expect(component['getSortIcon']('settingKey')).toBe('ti-arrows-sort');
    });

    it('should return ti-arrow-up for active ascending column', () => {
      component['sortDirection'] = 'asc';
      expect(component['getSortIcon']('category')).toBe('ti-arrow-up');
    });

    it('should return ti-arrow-down for active descending column', () => {
      component['sortDirection'] = 'desc';
      expect(component['getSortIcon']('category')).toBe('ti-arrow-down');
    });
  });

  describe('getCategoryClass', () => {
    it.each([
      ['SCHEDULING', 'bg-primary'],
      ['GITHUB', 'bg-dark'],
      ['MATOMO', 'bg-info'],
      ['MAIL', 'bg-success'],
      ['SECURITY', 'bg-danger'],
      ['CORS', 'bg-warning text-dark'],
      ['APPLICATION', 'bg-secondary'],
      ['UNKNOWN', 'bg-light text-dark']
    ])('should return correct class for %s', (category, expectedClass) => {
      expect(component['getCategoryClass'](category)).toBe(expectedClass);
    });
  });

  describe('toggleSecret / isSecretVisible', () => {
    it('should show secret after toggle on', () => {
      expect(component['isSecretVisible']('github.token')).toBe(false);
      component['toggleSecret']('github.token');
      expect(component['isSecretVisible']('github.token')).toBe(true);
    });

    it('should hide secret after second toggle', () => {
      component['toggleSecret']('github.token');
      component['toggleSecret']('github.token');
      expect(component['isSecretVisible']('github.token')).toBe(false);
    });

    it('should track visibility independently per key', () => {
      component['toggleSecret']('github.token');
      expect(component['isSecretVisible']('github.token')).toBe(true);
      expect(component['isSecretVisible']('app.name')).toBe(false);
    });
  });
});
