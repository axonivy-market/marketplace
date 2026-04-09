import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  flushMicrotasks,
  tick
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { DeprecationManagementComponent } from './deprecation-management.component';
import { ProductService } from '../../product/product.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminAuthService } from '../admin-auth.service';
import { PullRequestAction } from '../../../shared/enums/pullrequest-action';
import { DeprecatedResponse } from '../../../shared/models/deprecated-response';

describe('DeprecatedManagementComponent', () => {
  let component: DeprecationManagementComponent;
  let fixture: ComponentFixture<DeprecationManagementComponent>;
  let productService: jasmine.SpyObj<ProductService>;
  let adminAuthService: jasmine.SpyObj<AdminAuthService>;
  let originalClipboard: Clipboard | undefined;
  let writeTextSpy: jasmine.Spy;

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
    const productServiceSpy = jasmine.createSpyObj('ProductService', [
      'fetchAllProductIdsByDeprecated',
      'updateDeprecatedProduct'
    ]);
    const languageServiceSpy = jasmine.createSpyObj('LanguageService', [], {
      selectedLanguage: () => 'en'
    });
    const themeServiceSpy = jasmine.createSpyObj('ThemeService', ['isDarkMode']);
    const adminAuthServiceSpy = jasmine.createSpyObj('AdminAuthService', [
      'loadFromSessionStorage'
    ]);

    productServiceSpy.fetchAllProductIdsByDeprecated.and.returnValue(
      of(mockDeprecatedRows)
    );
    productServiceSpy.updateDeprecatedProduct.and.returnValue(
      of({ productDeprecations: mockDeprecatedRows, pullRequestUrl: null })
    );
    adminAuthServiceSpy.loadFromSessionStorage.and.returnValue(baseUserInfo as any);

    await TestBed.configureTestingModule({
      imports: [DeprecationManagementComponent, TranslateModule.forRoot()],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: LanguageService, useValue: languageServiceSpy },
        { provide: ThemeService, useValue: themeServiceSpy },
        { provide: AdminAuthService, useValue: adminAuthServiceSpy }
      ]
    }).compileComponents();

    productService = TestBed.inject(ProductService) as jasmine.SpyObj<ProductService>;
    adminAuthService = TestBed.inject(AdminAuthService) as jasmine.SpyObj<AdminAuthService>;

    fixture = TestBed.createComponent(DeprecationManagementComponent);
    component = fixture.componentInstance;

    originalClipboard = navigator.clipboard;
    writeTextSpy = jasmine
      .createSpy('writeText')
      .and.returnValue(Promise.resolve());
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

  it('should create and initialize rows from API', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    expect(component).toBeTruthy();
    expect(adminAuthService.loadFromSessionStorage).toHaveBeenCalled();
    expect(productService.fetchAllProductIdsByDeprecated).toHaveBeenCalledWith(true);
    expect(component.deprecatedItems).toEqual(mockDeprecatedRows);
    expect(component.filteredDeprecatedRows).toEqual(mockDeprecatedRows);
  }));

  it('should open deprecate dialog when trigger is called', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.trigger();

    expect(component.showDeprecatedProductDialog).toBeTrue();
    expect(component.deprecatedResponse.pullRequestUrl).toBeNull();
  }));

  it('should close deprecate dialog and reset fields after animation delay', fakeAsync(() => {
    component.moderatorName = 'alice';
    component.showDeprecatedProductDialog = true;
    component.isDeprecating = true;
    component.isCopySuccessVisible = true;
    component.deprecatedRequest = {
      successorUrl: 'https://market.axonivy.com/portal',
      addReadme: true,
      isDeprecated: true,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester: 'alice'
    };
    component.validationErrors = {
      productId: 'required',
      successorUrl: 'invalid'
    };

    component.closeDialog();

    expect(component.isClosing).toBeTrue();
    tick(250);

    expect(component.showDeprecatedProductDialog).toBeFalse();
    expect(component.isClosing).toBeFalse();
    expect(component.isDeprecating).toBeFalse();
    expect(component.isCopySuccessVisible).toBeFalse();
    expect(component.deprecatedRequest).toEqual({
      successorUrl: '',
      addReadme: false,
      isDeprecated: false,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester: 'alice'
    });
    expect(component.validationErrors).toEqual({});
  }));

  it('should validate form fields', () => {
    component.productId = '';
    expect(component.validateForm()).toBeFalse();
    expect(component.validationErrors.productId).toContain('required');

    component.productId = 'cms-live-editor';
    component.deprecatedRequest.successorUrl = 'invalid-url';
    expect(component.validateForm()).toBeFalse();
    expect(component.validationErrors.successorUrl).toContain('http:// or https://');

    component.deprecatedRequest.successorUrl = 'https://market.axonivy.com/portal';
    expect(component.validateForm()).toBeTrue();
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
    expect(component.deprecatedRequest.isDeprecated).toBeTrue();
    expect(component.deprecatedRequest.pullRequestAction).toBe(PullRequestAction.ADD);
    expect(component.dropdownOpen).toBeFalse();
  });

  it('should load ids and open dropdown', fakeAsync(() => {
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
    productService.fetchAllProductIdsByDeprecated.and.returnValue(
      of(selectableRows)
    );
    component.productId = 'vertex';

    component.openExtensionDropdown();
    flushMicrotasks();

    expect(productService.fetchAllProductIdsByDeprecated).toHaveBeenCalledWith(
      null
    );
    expect(component.selectableProductIds).toEqual([
      'cms-live-editor',
      'vertexai-google',
      'rtf-factory'
    ]);
    expect(component.filteredProductIds).toEqual(['vertexai-google']);
    expect(component.dropdownOpen).toBeTrue();
  }));

  it('should deprecate product and open success dialog', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    const response: DeprecatedResponse = {
      productDeprecations: mockDeprecatedRows,
      pullRequestUrl: 'https://github.com/org/repo/pull/123'
    };
    productService.updateDeprecatedProduct.and.returnValue(of(response));

    component.productId = 'cms-live-editor';
    component.deprecatedRequest.successorUrl = 'https://market.axonivy.com/portal';
    component.deprecatedRequest.isDeprecated = true;

    component.deprecatedProduct();
    tick();

    expect(productService.updateDeprecatedProduct).toHaveBeenCalled();
    expect(component.showDeprecatedProductDialog).toBeFalse();
    expect(component.showSuccessDialog).toBeTrue();
    expect(component.successMode).toBe('deprecate');
    expect(component.successPullRequestUrl).toBe('https://github.com/org/repo/pull/123');
  }));

  it('should fall back to refresh rows when update response has no productDeprecations', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    productService.updateDeprecatedProduct.and.returnValue(
      of({ productDeprecations: undefined, pullRequestUrl: null })
    );

    component.productId = 'cms-live-editor';
    component.deprecatedRequest.isDeprecated = true;

    component.deprecatedProduct();
    tick();

    expect(productService.fetchAllProductIdsByDeprecated).toHaveBeenCalledWith(true);
  }));

  it('should execute undeprecate flow with REMOVE action', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.removedProductId = 'cms-live-editor';

    component.executeRemoveDeprecation();
    tick();

    expect(productService.updateDeprecatedProduct).toHaveBeenCalledWith(
      'cms-live-editor',
      jasmine.objectContaining({
        pullRequestAction: PullRequestAction.REMOVE,
        isDeprecated: null
      }),
      'token-123'
    );
    expect(component.successMode).toBe('undeprecate');
    expect(component.showSuccessDialog).toBeTrue();
  }));

  it('should close success dialog and reset deprecate form in deprecate mode', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.successMode = 'deprecate';
    component.showSuccessDialog = true;
    component.productId = 'cms-live-editor';

    component.closeSuccessDialog();
    tick(250);

    expect(component.showSuccessDialog).toBeFalse();
    expect(component.successMode).toBeNull();
    expect(component.productId).toBe('');
  }));

  it('should evaluate pull request url correctly', () => {
    component.successPullRequestUrl = null;
    expect(component.hasPullRequestUrl()).toBeFalse();

    component.successPullRequestUrl = 'null';
    expect(component.hasPullRequestUrl()).toBeFalse();

    component.successPullRequestUrl = ' https://github.com/org/repo/pull/99 ';
    expect(component.hasPullRequestUrl()).toBeTrue();
  });

  it('should copy pull request url and show copy success message temporarily', fakeAsync(() => {
    component.successPullRequestUrl = ' https://github.com/org/repo/pull/99 ';

    component.copyPullRequestUrl();
    flushMicrotasks();

    expect(writeTextSpy).toHaveBeenCalledWith(
      'https://github.com/org/repo/pull/99'
    );
    expect(component.isCopySuccessVisible).toBeTrue();

    tick(1500);
    expect(component.isCopySuccessVisible).toBeFalse();
  }));

  it('should not copy when pull request url is empty or invalid', fakeAsync(() => {
    component.successPullRequestUrl = 'null';

    component.copyPullRequestUrl();
    flushMicrotasks();
    expect(writeTextSpy).not.toHaveBeenCalled();

    component.successPullRequestUrl = '   ';
    component.copyPullRequestUrl();
    flushMicrotasks();
    expect(writeTextSpy).not.toHaveBeenCalled();
    expect(component.isCopySuccessVisible).toBeFalse();
  }));
});
