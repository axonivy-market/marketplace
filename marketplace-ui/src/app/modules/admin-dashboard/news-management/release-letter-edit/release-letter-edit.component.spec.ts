import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
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

vi.mock('easymde', () => {
  class FakeEasyMDE {
    codemirror = {
      setOption() {},
      getCursor() { return { line: 0, ch: 0 }; },
      setCursor() {},
      getWrapperElement() {
        const container = document.createElement('div');
        container.classList.add('EasyMDEContainer');
        const toolbar = document.createElement('div');
        toolbar.classList.add('editor-toolbar');
        const cm = document.createElement('div');
        cm.classList.add('CodeMirror');
        container.appendChild(toolbar);
        container.appendChild(cm);
        // Append to document so querySelector works
        document.body.appendChild(container);
        return { closest: (_selector: string) => container } as any;
      },
      on() {}
    };
    private _value = '';
    constructor(public config: any) {}
    value(val?: string): any {
      if (val !== undefined) { this._value = val; }
      return this._value;
    }
    toTextArea() {}
    cleanup() {}
  }
  return { default: FakeEasyMDE };
});

const mockResponse = {
  id: '123',
  sprint: 'S43',
  content: 'content',
  latest: true,
  createdAt: '2026-02-01',
  updatedAt: '2026-02-02'
};

describe('ReleaseLetterEditComponent', () => {
  let component: ReleaseLetterEditComponent;
  let fixture: ComponentFixture<ReleaseLetterEditComponent>;

  let adminDashboardServiceMock: MockedObject<AdminDashboardService>;
  let routerMock: MockedObject<Router>;
  let activatedRouteMock: any;
  let pageTitleServiceMock: MockedObject<PageTitleService>;
  let translateServiceMock: MockedObject<TranslateService>;

  beforeEach(async () => {
    activatedRouteMock = {
      paramMap: of(convertToParamMap({}))
    };

    adminDashboardServiceMock = {
      getReleaseLetterById: vi
        .fn()
        .mockName('AdminDashboardService.getReleaseLetterById'),
      getReleaseLetterBySprint: vi
        .fn()
        .mockName('AdminDashboardService.getReleaseLetterBySprint'),
      createReleaseLetter: vi
        .fn()
        .mockName('AdminDashboardService.createReleaseLetter'),
      updateReleaseLetter: vi
        .fn()
        .mockName('AdminDashboardService.updateReleaseLetter')
    } as any;

    routerMock = {
      navigate: vi.fn().mockName('Router.navigate')
    } as any;

    translateServiceMock = {
      instant: vi.fn().mockName('TranslateService.instant'),
      get: vi.fn().mockName('TranslateService.get')
    } as any;

    translateServiceMock.instant.mockImplementation((key: string) => key);
    translateServiceMock.get.mockImplementation((key: string) => of(key));

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

    pageTitleServiceMock = {
      setTitleOnLangChange: vi
        .fn()
        .mockName('PageTitleService.setTitleOnLangChange'),
      translateService: translateServiceMock,
      titleService: undefined,
      setTitle: vi.fn().mockName('PageTitleService.setTitle'),
      ngOnDestroy: vi.fn().mockName('PageTitleService.ngOnDestroy')
    } as any;
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

    expect(component.isCreateMode).toBe(true);
    expect(pageTitleServiceMock.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.admin.newsManagement.pageTitle'
    );
  });

  it('should initialize in edit mode and load release letter', () => {
    adminDashboardServiceMock.getReleaseLetterById.mockReturnValue(
      of(mockResponse)
    );

    activatedRouteMock.paramMap = of(convertToParamMap({ id: '123' }));

    fixture = TestBed.createComponent(ReleaseLetterEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.isCreateMode).toBe(false);
    expect(component.selectedId).toBe('123');
  });

  it('should call createReleaseLetter in create mode', () => {
    vi.spyOn(component, 'createReleaseLetter').mockImplementation(() => {});

    component.isCreateMode = true;

    const event = new Event('submit');
    vi.spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.createReleaseLetter).toHaveBeenCalledWith(
      component.releaseLetter
    );
  });

  it('should call updateReleaseLetter in edit mode', () => {
    vi.spyOn(component, 'updateReleaseLetter').mockImplementation(() => {});

    component.isCreateMode = false;

    const event = new Event('submit');
    vi.spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(component.updateReleaseLetter).toHaveBeenCalledWith(
      component.releaseLetter
    );
  });

  it('should navigate after successful create', () => {
    adminDashboardServiceMock.createReleaseLetter.mockReturnValue(of(void 0));

    component.createReleaseLetter(component.releaseLetter);

    expect(routerMock.navigate).toHaveBeenCalledWith([
      '/internal-dashboard/news-management'
    ]);
    expect(component.isSubmitting()).toBe(false);
  });

  it('should handle error on create', () => {
    translateServiceMock.instant.mockReturnValue('translated');

    adminDashboardServiceMock.createReleaseLetter.mockReturnValue(
      throwError(() => ({
        error: { helpCode: 'SOME_ERROR' }
      }))
    );

    component.createReleaseLetter(component.releaseLetter);

    expect(component.genericErrorMessage).toBe('translated');
    expect(component.isSubmitting()).toBe(false);
  });

  it('should navigate after successful update', () => {
    adminDashboardServiceMock.updateReleaseLetter.mockReturnValue(
      of(mockResponse)
    );

    component.updateReleaseLetter(component.releaseLetter);

    expect(routerMock.navigate).toHaveBeenCalledWith([
      '/internal-dashboard/news-management'
    ]);
  });

  it('should set sprint error for blank sprint', () => {
    translateServiceMock.instant.mockReturnValue('blank error');

    component.handleError(SPRINT_CANNOT_BE_BLANK.toString());

    expect(component.sprintErrorMessage).toBe('blank error');
  });

  it('should set sprint error for existing sprint', () => {
    translateServiceMock.instant.mockReturnValue('exists error');

    component.handleError(
      RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString()
    );

    expect(component.sprintErrorMessage).toBe('exists error');
  });

  it('should set generic error for unknown code', () => {
    translateServiceMock.instant.mockReturnValue('generic error');

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

    vi.spyOn(component, 'createReleaseLetter');

    component.isCreateMode = true;

    const event = new Event('submit');
    vi.spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(component.createReleaseLetter).not.toHaveBeenCalled();
  });
});
