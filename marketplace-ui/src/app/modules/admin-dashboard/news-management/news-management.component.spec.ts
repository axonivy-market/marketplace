import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewsManagementComponent } from './news-management.component';
import { of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminDashboardService } from '../admin-dashboard.service';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ReleaseLetter } from '../../../shared/models/release-letter-request.model';

describe('NewsManagementComponent', () => {
  let component: NewsManagementComponent;
  let fixture: ComponentFixture<NewsManagementComponent>;

  let adminDashboardServiceMock: any;
  let appModalServiceMock: any;
  let pageTitleServiceMock: any;
  let routerMock: any;

  const mockReleaseLetters: ReleaseLetter[] = [
    {
      sprint: 'S1',
      content: 'Content 1',
      latest: true,
      createdAt: '2025-01-01'
    },
    {
      sprint: 'S2',
      content: 'Content 2',
      latest: false,
      createdAt: '2025-01-02'
    }
  ];

  const mockResponse = {
    _embedded: {
      releaseLetterModelList: mockReleaseLetters
    }
  };

  beforeEach(async () => {
    adminDashboardServiceMock = {
      getReleaseLettersWithoutPaging: jasmine
        .createSpy()
        .and.returnValue(of(mockResponse))
    };

    appModalServiceMock = {
      openReleaseLetterModal: jasmine.createSpy(),
      openDeleteReleaseLetterConfirmModal: jasmine
        .createSpy()
        .and.returnValue(Promise.resolve())
    };

    pageTitleServiceMock = {
      setTitleOnLangChange: jasmine.createSpy()
    };

    routerMock = {
      navigate: jasmine.createSpy()
    };

    await TestBed.configureTestingModule({
      imports: [NewsManagementComponent, TranslateModule.forRoot()],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: ActivatedRoute, useValue: {} },
        { provide: Router, useValue: routerMock },
        { provide: AdminDashboardService, useValue: adminDashboardServiceMock },
        { provide: AppModalService, useValue: appModalServiceMock },
        { provide: PageTitleService, useValue: pageTitleServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NewsManagementComponent);
    component = fixture.componentInstance;
    component.isBrowser = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set page title and load release letters on init', () => {
    component.ngOnInit();

    expect(pageTitleServiceMock.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.admin.newsManagement.pageTitle'
    );

    expect(
      adminDashboardServiceMock.getReleaseLettersWithoutPaging
    ).toHaveBeenCalled();
  });

  it('should load release letters into signal', () => {
    expect(component.releaseLetterList().length).toBe(2);
  });

  it('should append release letters if already existing', () => {
    component.releaseLetterList.set([{ sprint: 'OLD' } as any]);

    component.loadReleaseLetters();

    expect(component.releaseLetterList().length).toBe(3);
  });
});
