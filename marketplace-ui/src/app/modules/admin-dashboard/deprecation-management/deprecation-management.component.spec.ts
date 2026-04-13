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
import { of } from 'rxjs';

import { DeprecationManagementComponent } from './deprecation-management.component';
import { ProductService } from '../../product/product.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminAuthService } from '../admin-auth.service';
import { PullRequestAction } from '../../../shared/enums/pullrequest-action';

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
      updateDeprecatedProduct: vi.fn()
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
    productServiceSpy.updateDeprecatedProduct.mockReturnValue(
      of({ productDeprecations: mockDeprecatedRows, pullRequestUrl: null })
    );
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
      successorUrl: '',
      addReadme: false,
      isDeprecated: false,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester: 'alice'
    });
    expect(component.validationErrors).toEqual({});
    vi.useRealTimers();
  });

  it('should validate form fields', () => {
    component.productId = '';
    expect(component.validateForm()).toBe(false);
    expect(component.validationErrors.productId).toContain('required');

    component.productId = 'cms-live-editor';
    component.deprecationRequest.successorUrl = 'invalid-url';
    expect(component.validateForm()).toBe(false);
    expect(component.validationErrors.successorUrl).toContain('http:// or https://');

    component.deprecationRequest.successorUrl = 'https://market.axonivy.com/portal';
    expect(component.validateForm()).toBe(true);
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
      null
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
    expect(component.successMode).toBe('deprecate');
    expect(component.successPullRequestUrl).toBe('https://github.com/org/repo/pull/123');
  });

  it('should fall back to refresh rows when update response has no productDeprecations', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    productService.updateDeprecatedProduct.mockReturnValue(of(''));

    component.productId = 'cms-live-editor';
    component.deprecationRequest.isDeprecated = true;

    await component.deprecatedProduct();

    expect(productService.fetchAllProductIdsByDeprecated).toHaveBeenCalledWith(true);
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
      }),
      'token-123'
    );
    expect(component.successMode).toBe('undeprecate');
    expect(component.showSuccessDialog).toBe(true);
  });

  it('should close success dialog and reset deprecate form in deprecate mode', async () => {
    vi.useFakeTimers();
    fixture.detectChanges();
    await fixture.whenStable();

    component.successMode = 'deprecate';
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
});
