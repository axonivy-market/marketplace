import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MonitoringDashboardComponent } from './monitor-dashboard.component';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { of } from 'rxjs';
import { PLATFORM_ID } from '@angular/core';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
class MockPageTitleService {
  setTitleOnLangChange = jasmine.createSpy('setTitleOnLangChange');
}

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;
  let mockPageTitleService: MockPageTitleService;

  beforeEach(async () => {
    mockPageTitleService = new MockPageTitleService();

    await TestBed.configureTestingModule({
      imports: [
        MonitoringDashboardComponent,
        TranslateModule.forRoot(),
        RouterModule.forRoot([]),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: PageTitleService, useValue: mockPageTitleService },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        },
        TranslateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit (platform: browser)', () => {
    it('should call setTitleOnLangChange', () => {
      spyOn(component['route'].queryParams, 'subscribe').and.callThrough();

      component.ngOnInit();

      expect(mockPageTitleService.setTitleOnLangChange).toHaveBeenCalledWith(
        'common.monitor.dashboard.pageTitle'
      );
      expect(component['route'].queryParams.subscribe).toHaveBeenCalled();
    });
  });

  it('should change active tab when setActiveTab() is called', () => {
    component.setActiveTab(component.STANDARD_TAB);
    expect(component.activeTab).toBe(component.STANDARD_TAB);
  });
});
