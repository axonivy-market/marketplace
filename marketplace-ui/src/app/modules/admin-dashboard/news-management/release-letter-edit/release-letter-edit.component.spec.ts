import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { of, Subject, throwError } from 'rxjs';
import {
  RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED,
  SPRINT_CANNOT_BE_BLANK
} from '../../../../shared/constants/common.constant';
import { PageTitleService } from '../../../../shared/services/page-title.service';
import { AdminDashboardService } from '../../admin-dashboard.service';
import { ReleaseLetterEditComponent } from './release-letter-edit.component';

const mockResponse = {
  sprint: 'S43',
  content: 'content',
  latest: true,
  createdAt: '2026-02-01'
};

describe('ReleaseLetterEditComponent', () => {
  let component: ReleaseLetterEditComponent;
  let fixture: ComponentFixture<ReleaseLetterEditComponent>;

  let adminDashboardServiceMock: jasmine.SpyObj<AdminDashboardService>;
  let routerMock: jasmine.SpyObj<Router>;
  let activatedRouteMock: any;
  let pageTitleServiceMock: jasmine.SpyObj<PageTitleService>;
  let translateServiceMock: jasmine.SpyObj<TranslateService>;

  beforeEach(async () => {
    activatedRouteMock = {
      paramMap: of(convertToParamMap({}))
    };

    adminDashboardServiceMock = jasmine.createSpyObj('AdminDashboardService', [
      'getRelaseLetterBySprint',
      'createReleaseLetter',
      'updateReleaseLetter'
    ]);

    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    // Start of mocking TranslateService with instant and get methods, and onLangChange event
    translateServiceMock = jasmine.createSpyObj('TranslateService', [
      'instant',
      'get'
    ]);

    translateServiceMock.instant.and.callFake((key: string) => key);
    translateServiceMock.get.and.callFake((key: string) => of(key));

    const onLangChange = new Subject();
    const onTranslationChange = new Subject();
    const onDefaultLangChange = new Subject();

    Object.defineProperty(translateServiceMock, 'onLangChange', {
      value: onLangChange
    });

    Object.defineProperty(translateServiceMock, 'onTranslationChange', {
      value: onTranslationChange
    });

    Object.defineProperty(translateServiceMock, 'onDefaultLangChange', {
      value: onDefaultLangChange
    });

    pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', [
      'setTitleOnLangChange'
    ]);
    // End of mocking TranslateService

    await TestBed.configureTestingModule({
      imports: [ReleaseLetterEditComponent],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: AdminDashboardService, useValue: adminDashboardServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: PageTitleService, useValue: pageTitleServiceMock },
        { provide: TranslateService, useValue: translateServiceMock },
        {
          provide: ActivatedRoute,
          useValue: activatedRouteMock
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize in create mode when no sprint param', () => {
    fixture.detectChanges();

    expect(component.isCreateMode).toBeTrue();
    expect(pageTitleServiceMock.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.admin.newsManagement.pageTitle'
    );
  });

  it('should initialize in edit mode and load release letter', () => {
    adminDashboardServiceMock.getRelaseLetterBySprint.and.returnValue(
      of(mockResponse)
    );

    // Set param BEFORE component creation
    activatedRouteMock.paramMap = of(convertToParamMap({ sprint: 'S43' }));

    fixture = TestBed.createComponent(ReleaseLetterEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.isCreateMode).toBeFalse();
    expect(component.selectedSprint).toBe('S43');
  });

  it('should call createReleaseLetter in create mode', () => {
    spyOn(component, 'createReleaseLetter');

    component.isCreateMode = true;

    const event = new Event('submit');
    spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.createReleaseLetter).toHaveBeenCalledWith(
      component.releaseLetter
    );
  });

  it('should call updateReleaseLetter in edit mode', () => {
    spyOn(component, 'updateReleaseLetter');

    component.isCreateMode = false;
    component.selectedSprint = 'S43';

    const event = new Event('submit');
    spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(component.updateReleaseLetter).toHaveBeenCalledWith(
      'S43',
      component.releaseLetter
    );
  });

  it('should navigate after successful create', () => {
    adminDashboardServiceMock.createReleaseLetter.and.returnValue(
      of(mockResponse)
    );

    component.createReleaseLetter(component.releaseLetter);

    expect(routerMock.navigate).toHaveBeenCalledWith([
      '/internal-dashboard/news-management'
    ]);
    expect(component.isSubmitting()).toBeFalse();
  });

  it('should handle error on create', () => {
    translateServiceMock.instant.and.returnValue('translated');

    adminDashboardServiceMock.createReleaseLetter.and.returnValue(
      throwError(() => ({
        error: { helpCode: 'SOME_ERROR' }
      }))
    );

    component.createReleaseLetter(component.releaseLetter);

    expect(component.genericErrorMessage).toBe('translated');
    expect(component.isSubmitting()).toBeFalse();
  });

  it('should navigate after successful update', () => {
    adminDashboardServiceMock.updateReleaseLetter.and.returnValue(
      of(mockResponse)
    );

    component.updateReleaseLetter('S43', component.releaseLetter);

    expect(routerMock.navigate).toHaveBeenCalledWith([
      '/internal-dashboard/news-management'
    ]);
  });

  it('should set sprint error for blank sprint', () => {
    translateServiceMock.instant.and.returnValue('blank error');

    component.handleError(SPRINT_CANNOT_BE_BLANK.toString());

    expect(component.sprintErrorMessage).toBe('blank error');
  });

  it('should set sprint error for existing sprint', () => {
    translateServiceMock.instant.and.returnValue('exists error');

    component.handleError(
      RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString()
    );

    expect(component.sprintErrorMessage).toBe('exists error');
  });

  it('should set generic error for unknown code', () => {
    translateServiceMock.instant.and.returnValue('generic error');

    component.handleError('UNKNOWN');

    expect(component.genericErrorMessage).toBe('generic error');
  });

  it('should navigate back to news management', () => {
    component.onClickingBackToNewsManagementButton();

    expect(routerMock.navigate).toHaveBeenCalledWith([
      '/internal-dashboard/news-management'
    ]);
  });

  it('should reset sprint error message on version change', () => {
    component.sprintErrorMessage = 'error';
    component.onReleaseVersionChange();

    expect(component.sprintErrorMessage).toBeNull();
  });

  it('should not submit if already submitting', () => {
    component.isSubmitting.set(true);

    spyOn(component, 'createReleaseLetter');

    component.isCreateMode = true;

    const event = new Event('submit');
    spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(component.createReleaseLetter).not.toHaveBeenCalled();
  });
});
