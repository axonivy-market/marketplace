import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
  type MockedObject
} from 'vitest';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { DeprecationManagementComponent } from './deprecation-management.component';
import { ProductService } from '../../product/product.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminAuthService } from '../admin-auth.service';
import { PullRequestAction } from '../../../shared/enums/pullrequest-action';
import { DeprecationMode } from '../../../shared/enums/deprecation-mode.enum';
import { ArchiveAction } from '../../../shared/enums/archive-action.enum';

describe('DeprecationManagementComponent', () => {
  let component: DeprecationManagementComponent;
  let fixture: ComponentFixture<DeprecationManagementComponent>;
  let productService: MockedObject<ProductService>;
  let adminAuthService: MockedObject<AdminAuthService>;
  let originalClipboard: Clipboard | undefined;
  let writeTextSpy: ReturnType<typeof vi.fn>;

  const mockDeprecatedRows = [
    {
      id: 'cms-live-editor',
      deprecationDate: '2025-10-10T08:15:00Z',
      deprecationRequester: 'alice'
    },
    {
      id: 'rtf-factory',
      deprecationDate: null,
      deprecationRequester: null
    }
  ];

  const baseUserInfo = {
    login: 'alice',
    name: 'Alice',
    avatarUrl: 'https://avatar.example/alice.png',
    url: 'https://github.com/alice',
    username: 'alice',
    token: 'token-123'
  };

  beforeEach(async () => {
    const productServiceSpy = {
      fetchAllProductIdsByDeprecated: vi.fn(),
      updateDeprecatedProduct: vi.fn(),
      updateArchiveStatus: vi.fn()
    };
    const languageServiceSpy = {
      selectedLanguage: vi.fn().mockReturnValue('en')
    };
    const themeServiceSpy = { isDarkMode: vi.fn() };
    const adminAuthServiceSpy = {
      loadFromSessionStorage: vi.fn()
    };

    productServiceSpy.fetchAllProductIdsByDeprecated.mockReturnValue(
      of(mockDeprecatedRows)
    );
    productServiceSpy.updateDeprecatedProduct.mockReturnValue(of(''));
    productServiceSpy.updateArchiveStatus.mockReturnValue(of('OK'));
    adminAuthServiceSpy.loadFromSessionStorage.mockReturnValue(baseUserInfo as any);

    await TestBed.configureTestingModule({
      imports: [DeprecationManagementComponent, TranslateModule.forRoot()],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: LanguageService, useValue: languageServiceSpy },
        { provide: ThemeService, useValue: themeServiceSpy },
        { provide: AdminAuthService, useValue: adminAuthServiceSpy }
      ]
    }).compileComponents();

    productService = TestBed.inject(ProductService) as MockedObject<ProductService>;
    adminAuthService = TestBed.inject(AdminAuthService) as MockedObject<AdminAuthService>;

    fixture = TestBed.createComponent(DeprecationManagementComponent);
    component = fixture.componentInstance;

    originalClipboard = navigator.clipboard;
    writeTextSpy = vi.fn().mockReturnValue(Promise.resolve());
    Object.defineProperty(navigator, 'clipboard', {
      value: { writeText: writeTextSpy },
      configurable: true
    });
  });

  afterEach(() => {
    Object.defineProperty(navigator, 'clipboard', {
      value: originalClipboard,
      configurable: true
    });
  });

  it('should create and initialize rows from API', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component).toBeTruthy();
    expect(adminAuthService.loadFromSessionStorage).toHaveBeenCalled();
    expect(productService.fetchAllProductIdsByDeprecated).toHaveBeenCalledWith(true);
    expect(component.deprecatedItems).toEqual(mockDeprecatedRows);
    expect(component.filteredDeprecatedRows).toEqual(mockDeprecatedRows);
  });

  it('should open deprecate dialog when trigger is called', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.openDeprecationDialog();

    expect(component.showDeprecatedProductDialog).toBe(true);
    expect(component.successPullRequestUrl).toBeNull();
  });

  it('should close deprecate dialog and reset fields after animation delay', () => {
    vi.useFakeTimers();
    component.moderatorName = 'alice';
    component.showDeprecatedProductDialog = true;
    component.isDeprecating = true;
    component.isCopySuccessVisible = true;
    component.deprecationRequest = {
      hasAlternativeExtension: true,
      alternativeExtension: 'Portal',
      successorUrl: 'https://market.axonivy.com/portal',
      isAddReadme: true,
      isDeprecated: true,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester: 'alice'
    };
    component.validationErrors = {
      productId: 'required',
      successorUrl: 'invalid'
    };

    component.closeDialog();

    expect(component.isClosing).toBe(true);
    vi.advanceTimersByTime(250);

    expect(component.showDeprecatedProductDialog).toBe(false);
    expect(component.isClosing).toBe(false);
    expect(component.isDeprecating).toBe(false);
    expect(component.isCopySuccessVisible).toBe(false);
    expect(component.deprecationRequest).toEqual({
      hasAlternativeExtension: false,
      alternativeExtension: '',
      successorUrl: '',
      isAddReadme: false,
      isDeprecated: false,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester: 'alice',
      deprecationDate: null
    });
    expect(component.validationErrors).toEqual({});
    vi.useRealTimers();
  });

  it('should validate form fields', () => {
    component.productId = '';
    expect(component.validateForm()).toBe(false);
    expect(component.validationErrors.productId).toContain('extensionIdRequired');

    component.productId = 'cms-live-editor';
    component.deprecationRequest.hasAlternativeExtension = true;
    component.deprecationRequest.alternativeExtension = '';
    component.deprecationRequest.successorUrl = '';
    expect(component.validateForm()).toBe(false);
    expect(component.validationErrors.alternativeExtension).toContain('alternativeExtensionRequired');
    expect(component.validationErrors.successorUrl).toContain('successorRequired');

    component.deprecationRequest.alternativeExtension = 'Portal';
    component.deprecationRequest.successorUrl = 'invalid-url';
    expect(component.validateForm()).toBe(false);
    expect(component.validationErrors.successorUrl).toContain('invalidSuccessorUrl');

    component.deprecationRequest.successorUrl = 'https://market.axonivy.com/portal';
    expect(component.validateForm()).toBe(true);

    component.deprecationRequest.hasAlternativeExtension = false;
    component.deprecationRequest.alternativeExtension = 'Stale value';
    component.deprecationRequest.successorUrl = 'https://market.axonivy.com/portal';
    expect(component.validateForm()).toBe(true);
    expect(component.deprecationRequest.alternativeExtension).toBe('');
    expect(component.deprecationRequest.successorUrl).toBe('');
  });

  it('should filter deprecated table by product id', () => {
    component.deprecatedItems = mockDeprecatedRows;

    component.filterTable('cms');
    expect(component.filteredDeprecatedRows.length).toBe(1);
    expect(component.filteredDeprecatedRows[0].id).toBe('cms-live-editor');

    component.filterTable('');
    expect(component.filteredDeprecatedRows.length).toBe(2);
  });

  it('should set extension info when selecting a product', () => {
    component.dropdownOpen = true;

    component.selectExtension('cms-live-editor');

    expect(component.productId).toBe('cms-live-editor');
    expect(component.deprecationRequest.isDeprecated).toBe(true);
    expect(component.deprecationRequest.pullRequestAction).toBe(PullRequestAction.ADD);
    expect(component.dropdownOpen).toBe(false);
  });

  it('should load ids and open dropdown', async () => {
    const selectableRows = [
      {
        id: 'cms-live-editor',
        deprecationDate: null,
        deprecationRequester: null
      },
      {
        id: 'vertexai-google',
        deprecationDate: null,
        deprecationRequester: null
      },
      {
        id: 'rtf-factory',
        deprecationDate: null,
        deprecationRequester: null
      }
    ];
    productService.fetchAllProductIdsByDeprecated.mockReturnValue(
      of(selectableRows)
    );
    component.productId = 'vertex';

    await component.openExtensionDropdown();

    expect(productService.fetchAllProductIdsByDeprecated).toHaveBeenCalledWith(
      undefined
    );
    expect(component.selectableProductIds).toEqual([
      'cms-live-editor',
      'vertexai-google',
      'rtf-factory'
    ]);
    expect(component.filteredProductIds).toEqual(['vertexai-google']);
    expect(component.dropdownOpen).toBe(true);
  });

  it('should deprecate product and open success dialog', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    productService.updateDeprecatedProduct.mockReturnValue(of('https://github.com/org/repo/pull/123'));

    component.productId = 'cms-live-editor';
    component.deprecationRequest.successorUrl = 'https://market.axonivy.com/portal';
    component.deprecationRequest.isDeprecated = true;

    await component.deprecatedProduct();

    expect(productService.updateDeprecatedProduct).toHaveBeenCalled();
    expect(component.showDeprecatedProductDialog).toBe(false);
    expect(component.showSuccessDialog).toBe(true);
    expect(component.successMode).toBe(DeprecationMode.DEPRECATE);
    expect(component.successPullRequestUrl).toBe('https://github.com/org/repo/pull/123');
  });

  it('should open success dialog even when pull request url is empty', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    productService.updateDeprecatedProduct.mockReturnValue(of(''));

    component.productId = 'new-product';
    component.deprecationRequest.isDeprecated = true;

    await component.deprecatedProduct();

    expect(component.showSuccessDialog).toBe(true);
    expect(component.successPullRequestUrl).toBe('');
    expect(component.hasPullRequestUrl()).toBe(false);
  });

  it('should execute undeprecate flow with REMOVE action', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.productId = 'cms-live-editor';

    await component.executeRemoveDeprecation();

    expect(productService.updateDeprecatedProduct).toHaveBeenCalledWith(
      'cms-live-editor',
      expect.objectContaining({
        pullRequestAction: PullRequestAction.REMOVE,
        isDeprecated: null
      })
    );
    expect(component.successMode).toBe(DeprecationMode.UNDEPRECATE);
    expect(component.showSuccessDialog).toBe(true);
  });

  it('should close success dialog and reset deprecate form in deprecate mode', () => {
    vi.useFakeTimers();
    component.successMode = DeprecationMode.DEPRECATE;
    component.showSuccessDialog = true;
    component.productId = 'cms-live-editor';

    component.closeSuccessDialog();
    vi.advanceTimersByTime(250);

    expect(component.showSuccessDialog).toBe(false);
    expect(component.successMode).toBeNull();
    expect(component.productId).toBe('');
    vi.useRealTimers();
  });

  it('should evaluate pull request url correctly', () => {
    component.successPullRequestUrl = null;
    expect(component.hasPullRequestUrl()).toBe(false);

    component.successPullRequestUrl = 'null';
    expect(component.hasPullRequestUrl()).toBe(false);

    component.successPullRequestUrl = ' https://github.com/org/repo/pull/99 ';
    expect(component.hasPullRequestUrl()).toBe(true);
  });

  it('should copy pull request url and show copy success message temporarily', async () => {
    vi.useFakeTimers();
    component.successPullRequestUrl = ' https://github.com/org/repo/pull/99 ';

    await component.copyPullRequestUrl();

    expect(writeTextSpy).toHaveBeenCalledWith(
      'https://github.com/org/repo/pull/99'
    );
    expect(component.isCopySuccessVisible).toBe(true);

    vi.advanceTimersByTime(1500);
    expect(component.isCopySuccessVisible).toBe(false);
    vi.useRealTimers();
  });

  it('should not copy when pull request url is empty or invalid', async () => {
    component.successPullRequestUrl = 'null';

    await component.copyPullRequestUrl();
    expect(writeTextSpy).not.toHaveBeenCalled();

    component.successPullRequestUrl = '   ';
    await component.copyPullRequestUrl();
    expect(writeTextSpy).not.toHaveBeenCalled();
    expect(component.isCopySuccessVisible).toBe(false);
  });

  describe('confirmRemovedDeprecation', () => {
    it('should set productId and show remove deprecation confirm dialog', async () => {
      component.productId = '';
      component.showRemoveDeprecationConfirmDialog = false;

      await component.confirmRemovedDeprecation('cms-live-editor');

      expect(component.productId).toBe('cms-live-editor');
      expect(component.showRemoveDeprecationConfirmDialog).toBe(true);
    });
  });

  describe('toggleArchiveStatus', () => {
    it('should set archiveTargetRow, clear error message, and show archive confirm dialog', async () => {
      const row = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };
      component.archiveErrorMessage = 'previous error';
      component.showArchiveConfirmDialog = false;

      await component.toggleArchiveStatus(row);

      expect(component.archiveTargetRow).toBe(row);
      expect(component.archiveErrorMessage).toBe('');
      expect(component.showArchiveConfirmDialog).toBe(true);
    });
  });

  describe('closeArchiveConfirmDialog', () => {
    it('should not close when isArchiving is true', () => {
      vi.useFakeTimers();
      component.isArchiving = true;
      component.showArchiveConfirmDialog = true;
      component.archiveTargetRow = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };

      component.closeArchiveConfirmDialog();

      expect(component.isClosingArchiveDialog).toBe(false);
      vi.advanceTimersByTime(250);
      expect(component.showArchiveConfirmDialog).toBe(true);
      expect(component.archiveTargetRow).not.toBeNull();
      vi.useRealTimers();
    });

    it('should set isClosingArchiveDialog immediately and reset state after delay', () => {
      vi.useFakeTimers();
      component.isArchiving = false;
      component.showArchiveConfirmDialog = true;
      component.archiveTargetRow = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };

      component.closeArchiveConfirmDialog();

      expect(component.isClosingArchiveDialog).toBe(true);
      expect(component.showArchiveConfirmDialog).toBe(true);
      expect(component.archiveTargetRow).not.toBeNull();

      vi.advanceTimersByTime(250);

      expect(component.showArchiveConfirmDialog).toBe(false);
      expect(component.isClosingArchiveDialog).toBe(false);
      expect(component.archiveTargetRow).toBeNull();
      vi.useRealTimers();
    });
  });

  describe('closeRemoveDeprecationDialog', () => {
    it('should not close when isRemoving is true', () => {
      vi.useFakeTimers();
      component.isRemoving = true;
      component.showRemoveDeprecationConfirmDialog = true;
      component.productId = 'cms-live-editor';

      component.closeRemoveDeprecationDialog();

      expect(component.isClosingRemoveDeprecationDialog).toBe(false);
      vi.advanceTimersByTime(250);
      expect(component.showRemoveDeprecationConfirmDialog).toBe(true);
      expect(component.productId).toBe('cms-live-editor');
      vi.useRealTimers();
    });

    it('should set isClosingRemoveDeprecationDialog immediately and reset state after delay', () => {
      vi.useFakeTimers();
      component.isRemoving = false;
      component.showRemoveDeprecationConfirmDialog = true;
      component.productId = 'cms-live-editor';

      component.closeRemoveDeprecationDialog();

      expect(component.isClosingRemoveDeprecationDialog).toBe(true);
      expect(component.showRemoveDeprecationConfirmDialog).toBe(true);
      expect(component.productId).toBe('cms-live-editor');

      vi.advanceTimersByTime(250);

      expect(component.showRemoveDeprecationConfirmDialog).toBe(false);
      expect(component.isClosingRemoveDeprecationDialog).toBe(false);
      expect(component.productId).toBe('');
      vi.useRealTimers();
    });
  });

  describe('executeToggleArchive', () => {
    it('should return early when isArchiving is true', async () => {
      component.isArchiving = true;
      component.archiveTargetRow = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };

      await component.executeToggleArchive();

      expect(productService.updateArchiveStatus).not.toHaveBeenCalled();
    });

    it('should return early when archiveTargetRow is null', async () => {
      component.isArchiving = false;
      component.archiveTargetRow = null;

      await component.executeToggleArchive();

      expect(productService.updateArchiveStatus).not.toHaveBeenCalled();
    });

    it('should call updateArchiveStatus with ARCHIVE action when row is not archived', async () => {
      const row = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };
      component.archiveTargetRow = row;
      component.showArchiveConfirmDialog = true;

      await component.executeToggleArchive();

      expect(productService.updateArchiveStatus).toHaveBeenCalledWith('cms-live-editor', ArchiveAction.ARCHIVE);
      expect(component.showArchiveConfirmDialog).toBe(false);
      expect(component.isClosingArchiveDialog).toBe(false);
      expect(component.archiveTargetRow).toBeNull();
      expect(row.isArchived).toBe(true);
      expect(component.isArchiving).toBe(false);
    });

    it('should call updateArchiveStatus with UNARCHIVE action when row is archived', async () => {
      const row = { id: 'rtf-factory', deprecationDate: null, deprecationRequester: null, isArchived: true };
      component.archiveTargetRow = row;
      component.showArchiveConfirmDialog = true;

      await component.executeToggleArchive();

      expect(productService.updateArchiveStatus).toHaveBeenCalledWith('rtf-factory', ArchiveAction.UNARCHIVE);
      expect(component.showArchiveConfirmDialog).toBe(false);
      expect(component.archiveTargetRow).toBeNull();
      expect(row.isArchived).toBe(false);
      expect(component.isArchiving).toBe(false);
    });

    it('should set archiveErrorMessage on failure', async () => {
      const row = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };
      component.archiveTargetRow = row;

      const errorResponse = new HttpErrorResponse({
        error: JSON.stringify({ messageDetails: 'common.error.archive.failed' }),
        status: 500,
        statusText: 'Internal Server Error'
      });
      productService.updateArchiveStatus.mockReturnValue(throwError(() => errorResponse));

      await component.executeToggleArchive();

      expect(component.archiveErrorMessage).toBe('common.error.archive.failed');
      expect(component.isArchiving).toBe(false);
      expect(row.isArchived).toBe(false);
      expect(component.archiveTargetRow).not.toBeNull();
    });

    it('should use default error key when error body has no messageDetails', async () => {
      const row = { id: 'cms-live-editor', deprecationDate: null, deprecationRequester: null, isArchived: false };
      component.archiveTargetRow = row;

      const errorResponse = new HttpErrorResponse({
        error: 'some-raw-error',
        status: 400,
        statusText: 'Bad Request'
      });
      productService.updateArchiveStatus.mockReturnValue(throwError(() => errorResponse));

      await component.executeToggleArchive();

      expect(component.archiveErrorMessage).toBe('some-raw-error');
      expect(component.isArchiving).toBe(false);
    });
  });
});
