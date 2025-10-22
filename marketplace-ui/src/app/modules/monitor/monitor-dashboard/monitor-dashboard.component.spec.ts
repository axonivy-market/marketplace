// import { TestBed } from '@angular/core/testing';
// import { MonitoringDashboardComponent } from './monitor-dashboard.component';
// import { ActivatedRoute } from '@angular/router';
// import { of } from 'rxjs';
// import { PageTitleService } from '../../../shared/services/page-title.service';
// import {
//   FOCUSED_TAB,
//   STANDARD_TAB
// } from '../../../shared/constants/common.constant';
// import { PLATFORM_ID } from '@angular/core';
// import { TranslateService } from '@ngx-translate/core';
// import { HttpClientTestingModule } from '@angular/common/http/testing';
//
// describe('MonitoringDashboardComponent', () => {
//   let component: MonitoringDashboardComponent;
//   let pageTitleServiceSpy: jasmine.SpyObj<PageTitleService>;
//   let translateService: jasmine.SpyObj<TranslateService>;
//
//   beforeEach(() => {
//     pageTitleServiceSpy = jasmine.createSpyObj('PageTitleService', [
//       'setTitleOnLangChange'
//     ]);
//   });
//
//   function setup(platformId: any, queryParams: any) {
//     TestBed.configureTestingModule({
//       imports: [
//         MonitoringDashboardComponent,
//         HttpClientTestingModule, // <-- Add this!
//       ],
//       providers: [
//         { provide: PLATFORM_ID, useValue: platformId },
//         { provide: ActivatedRoute, useValue: { queryParams: of(queryParams) } },
//         { provide: PageTitleService, useValue: pageTitleServiceSpy },
//         { provide: TranslateService, useValue: jasmine.createSpyObj('TranslateService', ['get', 'instant', 'onLangChange']) }
//       ]
//     });
//     component = TestBed.createComponent(MonitoringDashboardComponent).componentInstance;
//   }
//
//   it('should set initialFilter and activeTab from queryParams on browser', () => {
//     setup('browser', { search: 'test-search' });
//     component.ngOnInit();
//
//     expect(component.initialFilter()).toBe('test-search');
//     expect(component.activeTab).toBe(STANDARD_TAB);
//     expect(pageTitleServiceSpy.setTitleOnLangChange).toHaveBeenCalledWith(
//       'common.monitor.dashboard.pageTitle'
//     );
//   });
//
//   it('should not set initialFilter if search param is absent', () => {
//     setup('browser', {});
//     component.ngOnInit();
//
//     expect(component.initialFilter()).toBe('');
//     expect(component.activeTab).toBe(component.FOCUSED_TAB); // unchanged
//     expect(pageTitleServiceSpy.setTitleOnLangChange).toHaveBeenCalledWith(
//       'common.monitor.dashboard.pageTitle'
//     );
//   });
//
//   it('should set isLoading to false when not in browser', () => {
//     setup('server', {});
//     component.isLoading = true;
//     component.ngOnInit();
//
//     expect(component.isLoading).toBe(false);
//     expect(pageTitleServiceSpy.setTitleOnLangChange).not.toHaveBeenCalled();
//   });
// });
