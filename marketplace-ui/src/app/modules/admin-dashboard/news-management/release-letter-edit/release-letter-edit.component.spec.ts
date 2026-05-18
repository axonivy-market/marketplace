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
import { ReleaseLetterEditComponent } from './release-letter-edit.component';
import { NewsManagementService } from '../news-management.service';

vi.mock('easymde', () => {
  class FakeEasyMDE {
    codemirror = {
      setOption() {},
      getCursor() {
        return { line: 0, ch: 0 };
      },
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
      if (val !== undefined) {
        this._value = val;
      }
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

  let newsManagementServiceMock: MockedObject<NewsManagementService>;
  let routerMock: MockedObject<Router>;
  let activatedRouteMock: any;
  let pageTitleServiceMock: MockedObject<PageTitleService>;
  let translateServiceMock: MockedObject<TranslateService>;

  beforeEach(async () => {
    activatedRouteMock = {
      paramMap: of(convertToParamMap({}))
    };

    newsManagementServiceMock = {
      getReleaseLetterById: vi.fn().mockName('NewsManagementService.getReleaseLetterById'),
      getReleaseLetterBySprint: vi.fn().mockName('NewsManagementService.getReleaseLetterBySprint'),
      createReleaseLetter: vi.fn().mockName('NewsManagementService.createReleaseLetter'),
      updateReleaseLetter: vi.fn().mockName('NewsManagementService.updateReleaseLetter'),
      getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId: vi
        .fn()
        .mockName('NewsManagementService.getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId')
    } as any;

    routerMock = {
      navigate: vi.fn().mockName('Router.navigate')
    } as any;

    translateServiceMock = {
      instant: vi.fn().mockName('TranslateService.instant'),
      get: vi.fn().mockName('TranslateService.get')
    } as any;

    translateServiceMock.instant.mockImplementation((key: string | string[]) => key as string);
    translateServiceMock.get.mockImplementation((key: string | string[]) => of(key as string));

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
      setTitleOnLangChange: vi.fn().mockName('PageTitleService.setTitleOnLangChange')
    } as any;
    // End of mocking TranslateService

    await TestBed.configureTestingModule({
      imports: [ReleaseLetterEditComponent],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: NewsManagementService, useValue: newsManagementServiceMock },
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
    expect(pageTitleServiceMock.setTitleOnLangChange).toHaveBeenCalledWith('common.admin.newsManagement.pageTitle');
  });

  it('should initialize in edit mode and load release letter', () => {
    newsManagementServiceMock.getReleaseLetterById.mockReturnValue(of(mockResponse));

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
    expect(component.createReleaseLetter).toHaveBeenCalledWith(component.releaseLetter);
  });

  it('should call updateReleaseLetter in edit mode', () => {
    vi.spyOn(component, 'updateReleaseLetter').mockImplementation(() => {});

    component.isCreateMode = false;

    const event = new Event('submit');
    vi.spyOn(event, 'preventDefault');

    component.onSubmit(event);

    expect(component.updateReleaseLetter).toHaveBeenCalledWith(component.releaseLetter);
  });

  it('should navigate after successful create', () => {
    newsManagementServiceMock.createReleaseLetter.mockReturnValue(of(void 0));

    component.createReleaseLetter(component.releaseLetter);

    expect(routerMock.navigate).toHaveBeenCalledWith(['/internal-dashboard/news-management']);
    expect(component.isSubmitting()).toBe(false);
  });

  it('should handle error on create', () => {
    translateServiceMock.instant.mockReturnValue('translated');

    newsManagementServiceMock.createReleaseLetter.mockReturnValue(
      throwError(() => ({
        error: { helpCode: 'SOME_ERROR' }
      }))
    );

    component.createReleaseLetter(component.releaseLetter);

    expect(component.genericErrorMessage).toBe('translated');
    expect(component.isSubmitting()).toBe(false);
  });

  it('should navigate after successful update', () => {
    newsManagementServiceMock.updateReleaseLetter.mockReturnValue(of(mockResponse));

    component.updateReleaseLetter(component.releaseLetter);

    expect(routerMock.navigate).toHaveBeenCalledWith(['/internal-dashboard/news-management']);
  });

  it('should set sprint error for blank sprint', () => {
    translateServiceMock.instant.mockReturnValue('blank error');

    component.handleError(SPRINT_CANNOT_BE_BLANK.toString());

    expect(component.sprintErrorMessage).toBe('blank error');
  });

  it('should set sprint error for existing sprint', () => {
    translateServiceMock.instant.mockReturnValue('exists error');

    component.handleError(RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString());

    expect(component.sprintErrorMessage).toBe('exists error');
  });

  it('should set generic error for unknown code', () => {
    translateServiceMock.instant.mockReturnValue('generic error');

    component.handleError('UNKNOWN');

    expect(component.genericErrorMessage).toBe('generic error');
  });

  it('should navigate back to news management', () => {
    component.onClickingBackToNewsManagementButton();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/internal-dashboard/news-management']);
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

  it('should call updateReleaseLetter service and navigate on success', () => {
    const releaseLetter = {
      ...component.releaseLetter,
      id: '123'
    };

    newsManagementServiceMock.updateReleaseLetter.mockReturnValue(of(mockResponse));

    component.updateReleaseLetter(releaseLetter);

    expect(newsManagementServiceMock.updateReleaseLetter).toHaveBeenCalledWith('123', releaseLetter);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/internal-dashboard/news-management']);
    expect(component.isSubmitting()).toBe(false);
  });

  it('should handle error when updateReleaseLetter fails', () => {
    translateServiceMock.instant.mockReturnValue('translated error');

    const releaseLetter = {
      ...component.releaseLetter,
      id: '123'
    };

    newsManagementServiceMock.updateReleaseLetter.mockReturnValue(
      throwError(() => ({
        error: {
          helpCode: 'UNKNOWN_ERROR'
        }
      }))
    );

    component.updateReleaseLetter(releaseLetter);

    expect(newsManagementServiceMock.updateReleaseLetter).toHaveBeenCalledWith('123', releaseLetter);
    expect(component.genericErrorMessage).toBe('translated error');
    expect(component.isSubmitting()).toBe(false);
  });

  it('should set sprint error when updateReleaseLetter returns sprint blank error', () => {
    translateServiceMock.instant.mockReturnValue('sprint blank error');

    const releaseLetter = {
      ...component.releaseLetter,
      id: '123'
    };

    newsManagementServiceMock.updateReleaseLetter.mockReturnValue(
      throwError(() => ({
        error: {
          helpCode: SPRINT_CANNOT_BE_BLANK.toString()
        }
      }))
    );

    component.updateReleaseLetter(releaseLetter);

    expect(component.sprintErrorMessage).toBe('sprint blank error');
    expect(component.isSubmitting()).toBe(false);
  });

  it('should set sprint exists error when updateReleaseLetter returns duplicated sprint error', () => {
    translateServiceMock.instant.mockReturnValue('sprint existed error');

    const releaseLetter = {
      ...component.releaseLetter,
      id: '123'
    };

    newsManagementServiceMock.updateReleaseLetter.mockReturnValue(
      throwError(() => ({
        error: {
          helpCode: RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString()
        }
      }))
    );

    component.updateReleaseLetter(releaseLetter);

    expect(component.sprintErrorMessage).toBe('sprint existed error');
    expect(component.isSubmitting()).toBe(false);
  });

  it('should call saveAsDraft service and navigate on success', () => {
    const draftReleaseLetter = {
      ...component.releaseLetter,
      content: '',
      draftContent: component.releaseLetter.content
    };

    newsManagementServiceMock.saveAsDraft = vi.fn().mockReturnValue(of(mockResponse));

    component.saveAsDraft();

    expect(newsManagementServiceMock.saveAsDraft).toHaveBeenCalledWith(draftReleaseLetter);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/internal-dashboard/news-management']);
    expect(component.isSavingAsDraft()).toBe(false);
  });

  it('should handle error when saveAsDraft fails', () => {
    translateServiceMock.instant.mockReturnValue('translated error');

    newsManagementServiceMock.saveAsDraft = vi.fn().mockReturnValue(
      throwError(() => ({
        error: {
          helpCode: 'UNKNOWN_ERROR'
        }
      }))
    );

    component.saveAsDraft();

    expect(newsManagementServiceMock.saveAsDraft).toHaveBeenCalled();
    expect(component.genericErrorMessage).toBe('translated error');
    expect(component.isSavingAsDraft()).toBe(false);
  });

  it('should set sprint error when saveAsDraft returns sprint blank error', () => {
    translateServiceMock.instant.mockReturnValue('sprint blank error');

    newsManagementServiceMock.saveAsDraft = vi.fn().mockReturnValue(
      throwError(() => ({
        error: {
          helpCode: SPRINT_CANNOT_BE_BLANK.toString()
        }
      }))
    );

    component.saveAsDraft();

    expect(component.sprintErrorMessage).toBe('sprint blank error');
    expect(component.isSavingAsDraft()).toBe(false);
  });

  it('should set sprint exists error when saveAsDraft returns duplicated sprint error', () => {
    translateServiceMock.instant.mockReturnValue('sprint existed error');

    newsManagementServiceMock.saveAsDraft = vi.fn().mockReturnValue(
      throwError(() => ({
        error: {
          helpCode: RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString()
        }
      }))
    );

    component.saveAsDraft();

    expect(component.sprintErrorMessage).toBe('sprint existed error');
    expect(component.isSavingAsDraft()).toBe(false);
  });

  it('should not save draft if already saving as draft', () => {
    component.isSavingAsDraft.set(true);

    newsManagementServiceMock.saveAsDraft = vi.fn();

    component.saveAsDraft();

    expect(newsManagementServiceMock.saveAsDraft).not.toHaveBeenCalled();
  });

  it('should set draftContent before saving draft', () => {
    component.releaseLetter.content = 'draft content';

    newsManagementServiceMock.saveAsDraft = vi.fn().mockReturnValue(of(mockResponse));

    component.saveAsDraft();

    expect(component.releaseLetter.draftContent).toBe('draft content');
  });

  it('should set sprintCannotBeBlank error message when help code is SPRINT_CANNOT_BE_BLANK', () => {
    translateServiceMock.instant.mockReturnValue('sprint cannot be blank');

    component.handleError(SPRINT_CANNOT_BE_BLANK.toString());

    expect(translateServiceMock.instant).toHaveBeenCalledWith(
      'common.admin.releaseLetterEdit.sprintCannotBeBlankErrorMessage'
    );
    expect(component.sprintErrorMessage).toBe('sprint cannot be blank');
    expect(component.genericErrorMessage).toBeNull();
  });

  it('should set sprintAlreadyExists error message when help code is RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED', () => {
    translateServiceMock.instant.mockReturnValue('sprint already exists');

    component.handleError(RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString());

    expect(translateServiceMock.instant).toHaveBeenCalledWith(
      'common.admin.releaseLetterEdit.sprintAlreadyExistsErrorMessage'
    );
    expect(component.sprintErrorMessage).toBe('sprint already exists');
    expect(component.genericErrorMessage).toBeNull();
  });

  it('should set generic error message for unknown help code', () => {
    translateServiceMock.instant.mockReturnValue('generic error');

    component.handleError('UNKNOWN_ERROR');

    expect(translateServiceMock.instant).toHaveBeenCalledWith('common.admin.releaseLetterEdit.genericErrorMessage');
    expect(component.genericErrorMessage).toBe('generic error');
  });

  it('should overwrite previous sprint error when unknown error occurs', () => {
    component.sprintErrorMessage = 'old sprint error';

    translateServiceMock.instant.mockReturnValue('generic error');

    component.handleError('UNKNOWN_ERROR');

    expect(component.genericErrorMessage).toBe('generic error');
  });

  it('should overwrite previous generic error when sprint blank error occurs', () => {
    component.genericErrorMessage = 'old generic error';

    translateServiceMock.instant.mockReturnValue('blank sprint error');

    component.handleError(SPRINT_CANNOT_BE_BLANK.toString());

    expect(component.sprintErrorMessage).toBe('blank sprint error');
  });

  it('should handle RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED error', () => {
    translateServiceMock.instant.mockReturnValue('already existed');

    component.handleError(RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString());

    expect(translateServiceMock.instant).toHaveBeenCalledWith(
      'common.admin.releaseLetterEdit.sprintAlreadyExistsErrorMessage'
    );

    expect(component.sprintErrorMessage).toBe('already existed');
  });

  it('should handle unknown error code with generic message', () => {
    translateServiceMock.instant.mockReturnValue('generic error');

    component.handleError('UNKNOWN_ERROR');

    expect(translateServiceMock.instant).toHaveBeenCalledWith('common.admin.releaseLetterEdit.genericErrorMessage');

    expect(component.genericErrorMessage).toBe('generic error');
  });
});
