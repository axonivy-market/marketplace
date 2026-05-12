import { vi, describe, it, expect, beforeEach } from 'vitest';
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
import { NewsManagementService } from './news-management.service';

describe('NewsManagementComponent', () => {
  let component: NewsManagementComponent;
  let fixture: ComponentFixture<NewsManagementComponent>;

  let adminDashboardServiceMock: any;
  let newsManagementServiceMock: any;
  let appModalServiceMock: any;
  let pageTitleServiceMock: any;
  let routerMock: any;

  const mockReleaseLetters: ReleaseLetter[] = [
    {
      id: '123',
      sprint: 'S1',
      content: 'Content 1',
      latest: true,
      createdAt: '2025-01-01',
      updatedAt: '2025-01-02'
    },
    {
      id: '345',
      sprint: 'S2',
      content: 'Content 2',
      latest: false,
      createdAt: '2025-01-02',
      updatedAt: '2025-01-02'
    }
  ];

  const mockResponse = {
    _embedded: {
      releaseLetterModelList: mockReleaseLetters
    }
  };

  beforeEach(async () => {
    adminDashboardServiceMock = {
      getReleaseLetters: vi.fn().mockReturnValue(of(mockResponse))
    };

    newsManagementServiceMock = {
      getReleaseLetters: vi.fn().mockReturnValue(of(mockResponse))
    };

    appModalServiceMock = {
      openReleaseLetterModal: vi.fn(),
      openDeleteReleaseLetterConfirmModal: vi
        .fn()
        .mockReturnValue(Promise.resolve())
    };

    pageTitleServiceMock = {
      setTitleOnLangChange: vi.fn()
    };

    routerMock = {
      navigate: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NewsManagementComponent, TranslateModule.forRoot()],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: ActivatedRoute, useValue: {} },
        { provide: Router, useValue: routerMock },
        { provide: AdminDashboardService, useValue: adminDashboardServiceMock },
        { provide: NewsManagementService, useValue: newsManagementServiceMock },
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

    // expect(adminDashboardServiceMock.getReleaseLetters).toHaveBeenCalled();
    expect(newsManagementServiceMock.getReleaseLetters).toHaveBeenCalled();
  });

  it('should load release letters into signal', () => {
    expect(component.releaseLetterList().length).toBe(2);
  });

  it('should append release letters if already existing', () => {
    component.releaseLetterList.set([{ sprint: 'OLD' } as any]);

    component.loadReleaseLetters();

    expect(component.releaseLetterList().length).toBe(3);
  });

  it('should navigate to edit page', () => {
    component.navigateToEditPage('S1');

    expect(routerMock.navigate).toHaveBeenCalledWith(['edit', 'S1'], {
      relativeTo: component.route
    });
  });

  it('should format date correctly', () => {
    const result = component.formatDate('2025-01-01');

    expect(result).toBeTruthy();
  });

  it('should open release letter modal', () => {
    const sprint = mockReleaseLetters[0].sprint;

    vi.spyOn(document, 'activeElement', 'get').mockReturnValue({
      blur: vi.fn()
    } as any);

    component.openModal(sprint);

    expect(appModalServiceMock.openReleaseLetterModal).toHaveBeenCalledWith(
      sprint
    );
  });

  it('should refresh list after delete confirmation', async () => {
    component.releaseLetterList.set([]);

    await component.openDeleteConfirmModal(mockReleaseLetters[0]);

    expect(
      appModalServiceMock.openDeleteReleaseLetterConfirmModal
    ).toHaveBeenCalledWith(mockReleaseLetters[0]);

    // expect(adminDashboardServiceMock.getReleaseLetters).toHaveBeenCalled();
    expect(newsManagementServiceMock.getReleaseLetters).toHaveBeenCalled();

    expect(component.releaseLetterList()).toEqual(mockReleaseLetters);
  });

  it('should unsubscribe on destroy', () => {
    component.loadReleaseLetters();

    vi.spyOn(component.subscriptions[0], 'unsubscribe');

    component.ngOnDestroy();

    expect(component.subscriptions[0].unsubscribe).toHaveBeenCalled();
  });

  it('should do nothing if backend response is null', () => {
    // adminDashboardServiceMock.getReleaseLetters.mockReturnValue(of(null));
    newsManagementServiceMock.getReleaseLetters.mockReturnValue(of(null));

    component.releaseLetterList.set([]);
    component.subscriptions = [];

    component.loadReleaseLetters();

    expect(component.releaseLetterList().length).toBe(0);
  });
});
