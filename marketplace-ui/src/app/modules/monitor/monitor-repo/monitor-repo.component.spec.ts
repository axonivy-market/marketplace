// import {
//   ComponentFixture,
//   fakeAsync,
//   TestBed,
//   tick
// } from '@angular/core/testing';
// import { MonitoringRepoComponent } from './monitor-repo.component';
// import { Repository, TestResult } from '../github.service';
// import { HttpClientTestingModule } from '@angular/common/http/testing';
// import { LanguageService } from '../../../core/services/language/language.service';
// import { TranslateModule, TranslateService } from '@ngx-translate/core';
// import { ActivatedRoute } from '@angular/router';
// import { of } from 'rxjs';
// import { MatomoTestingModule } from 'ngx-matomo-client/testing';
// import { By } from '@angular/platform-browser';
// import { ASCENDING, DEFAULT_MODE, FOCUSED_TAB, REPORT_MODE } from '../../../shared/constants/common.constant';
//
// describe('MonitoringRepoComponent', () => {
//   let component: MonitoringRepoComponent;
//   let fixture: ComponentFixture<MonitoringRepoComponent>;
//   let mockRepositories: Repository[];
//
//   beforeEach(async () => {
//     mockRepositories = [
//       {
//         repoName: 'repo1',
//         productId: 'id1',
//         htmlUrl: 'https://github.com/user/repo1',
//         workflowInformation: [
//           {
//             workflowType: 'CI',
//             lastBuilt: new Date('2025-07-20T12:00:00Z'),
//             conclusion: 'success',
//             lastBuiltRunUrl:
//               'https://github.com/market/rtf-factory/actions/runs/11111'
//           },
//           {
//             workflowType: 'DEV',
//             lastBuilt: new Date('2025-07-21T12:00:00Z'),
//             conclusion: 'failure',
//             lastBuiltRunUrl:
//               'https://github.com/market/rtf-factory/actions/runs/11111'
//           }
//         ],
//         focused: true,
//         testResults: [
//           {
//             workflow: 'CI',
//             results: { PASSED: 20 }
//           } as TestResult
//         ]
//       },
//       {
//         repoName: 'repo2',
//         productId: 'id2',
//         htmlUrl: 'https://github.com/user/repo2',
//         workflowInformation: [],
//         focused: false,
//         testResults: []
//       },
//       {
//         repoName: 'repo3',
//         productId: 'id3',
//         htmlUrl: 'https://github.com/user/repo3',
//         workflowInformation: [],
//         focused: false,
//         testResults: []
//       }
//     ];
//
//     await TestBed.configureTestingModule({
//       imports: [
//         MonitoringRepoComponent,
//         HttpClientTestingModule,
//         TranslateModule.forRoot(),
//         MatomoTestingModule.forRoot()
//       ],
//       providers: [
//         {
//           provide: ActivatedRoute,
//           useValue: {
//             snapshot: {},
//             params: of({}),
//             queryParams: of({}),
//             data: of({})
//           }
//         },
//         LanguageService,
//         TranslateService
//       ]
//     }).compileComponents();
//
//     fixture = TestBed.createComponent(MonitoringRepoComponent);
//     component = fixture.componentInstance;
//
//     component.tabKey = FOCUSED_TAB;
//     component.repositoryPages = [...mockRepositories];
//     component.ngOnChanges();
//     component.refreshPagination();
//
//     fixture.detectChanges();
//   });
//
//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });
//
//   it('should initialize mode to default for given tabKey', () => {
//     component.ngOnInit();
//     expect(component.mode[FOCUSED_TAB]).toBe(DEFAULT_MODE);
//   });
//
//   it('should filter repositories when search changes', () => {
//     component.onSearchChanged('repo1');
//     expect(component.filteredRepositories.length).toBe(1);
//     expect(component.filteredRepositories[0].repoName).toBe('repo1');
//
//     component.onSearchChanged('asanaaaa');
//     expect(component.filteredRepositories.length).toBe(0);
//   });
//
//   it('should calculate pagination correctly', () => {
//     component.pageSize = 2;
//     component.refreshPagination();
//     expect(component.displayedRepositories.length).toBe(2);
//     component.page = 2;
//     component.refreshPagination();
//     expect(component.displayedRepositories.length).toBe(1);
//   });
//
//   it('should return correct page size for normal and "all"', () => {
//     component.pageSize = 2;
//     expect(component.getPageSize()).toBe(2);
//
//     component.pageSize = -1;
//     component.filteredRepositories = mockRepositories;
//     expect(component.getPageSize()).toBe(3);
//
//     component.filteredRepositories = [];
//     expect(component.getPageSize()).toBe(1);
//   });
//
//   it('should return collection size correctly', () => {
//     component.filteredRepositories = mockRepositories.slice(0, 2);
//     expect(component.getCollectionSize()).toBe(2);
//   });
//
//   it('should show all repositories when pageSize = -1', () => {
//     component.pageSize = -1;
//     component.filteredRepositories = [...mockRepositories];
//     component.refreshPagination();
//     expect(component.displayedRepositories.length).toBe(
//       mockRepositories.length
//     );
//   });
//
//   it('should sort repositories by name ascending/descending', () => {
//     component.sortColumn = component.COLUMN_NAME;
//     component.sortDirection = ASCENDING;
//     // Descending
//     component.sortRepositoriesByColumn(component.COLUMN_NAME);
//     expect(component.filteredRepositories.map(r => r.repoName)).toEqual([
//       'repo3', 'repo2', 'repo1' ]);
//
//     // Ascending
//     component.sortRepositoriesByColumn(component.COLUMN_NAME);
//     expect(component.filteredRepositories.map(r => r.repoName)).toEqual([
//       'repo1', 'repo2', 'repo3' ]);
//   });
//
//   it('should return correct market URL', () => {
//     const url = component.getMarketUrl('repo1');
//     expect(url).toContain(encodeURIComponent('repo1'));
//   });
//
//   it('should return correct workflow match', () => {
//     const repo = mockRepositories[0];
//     const match = component.findWorkflowMatch(repo, 'CI');
//     expect(match).toBeTruthy();
//     expect(match!.workflowType).toBe('CI');
//
//     const noMatch = component.findWorkflowMatch(repo, 'E2E');
//     expect(noMatch).toBeUndefined();
//   });
//
//   it('should toggle mode via ngModel binding', fakeAsync(() => {
//     component.mode[FOCUSED_TAB] = DEFAULT_MODE;
//     fixture.detectChanges();
//     const reportRadio = fixture.debugElement.query(
//       By.css('#report-mode-focused')
//     ).nativeElement as HTMLInputElement;
//
//     reportRadio.click();
//     fixture.detectChanges();
//     tick();
//     expect(component.mode[FOCUSED_TAB]).toBe(REPORT_MODE);
//   }));
//
//   it('should display repository links correctly in template', () => {
//     const repoLinks = fixture.debugElement.queryAll(By.css('#product-name'));
//
//     expect(repoLinks.length).toBe(3);
//     expect(repoLinks[0].nativeElement.textContent.trim()).toBe('repo1');
//     expect(repoLinks[1].nativeElement.textContent.trim()).toBe('repo2');
//     expect(repoLinks[2].nativeElement.textContent.trim()).toBe('repo3');
//   });
//
//   it('should show no-repositories message when filtered list is empty', () => {
//     component.onSearchChanged('asanaaaa');
//     fixture.detectChanges();
//     const noRepositoriesMessage = fixture.debugElement.query(
//       By.css('.no-repositories')
//     );
//     expect(noRepositoriesMessage).toBeTruthy();
//     expect(noRepositoriesMessage.nativeElement.textContent).toContain(
//       'common.monitor.dashboard.noRepositories'
//     );
//   });
//
//   it('should update sort icons correctly', () => {
//     const header = fixture.debugElement.query(By.css('th h5.table-header'));
//     expect(header.nativeElement.className).toContain('bi-arrow-up');
//
//     component.sortRepositoriesByColumn(component.COLUMN_NAME);
//     fixture.detectChanges();
//     expect(header.nativeElement.className).toContain('bi-arrow-down');
//   });
// });
