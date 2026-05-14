import { vi, type Mock, type MockedObject } from 'vitest';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { By, DomSanitizer, SafeHtml, Title } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of, Observable } from 'rxjs';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import {
  MOCK_PRODUCT_DETAIL,
  MOCK_CRON_JOB_PRODUCT_DETAIL,
  MOCK_PRODUCT_MODULE_CONTENT,
  MOCK_PRODUCTS,
  MOCK_FEEDBACK_API_RESPONSE
} from '../../../shared/mocks/mock-data';
import { ProductService } from '../product.service';
import { ProductDetailComponent } from './product-detail.component';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';
import { RoutingQueryParamService } from '../../../shared/services/routing.query.param.service';
import { MockProductService } from '../../../shared/mocks/mock-services';
import { ProductDetailActionType } from '../../../shared/enums/product-detail-action-type';
import { LanguageService } from '../../../core/services/language/language.service';
import { Language } from '../../../shared/enums/language.enum';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { AuthService } from '../../../auth/auth.service';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { ProductFeedbackService } from './product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { ProductStarRatingService } from './product-detail-feedback/product-star-rating-panel/product-star-rating.service';
import { FeedbackApiResponse } from '../../../shared/models/apis/feedback-response.model';
import { StarRatingCounting } from '../../../shared/models/star-rating-counting.model';
import { Feedback } from '../../../shared/models/feedback.model';
import MarkdownIt from 'markdown-it';
import {
  GITHUB_PULL_REQUEST_NUMBER_REGEX,
  PRODUCT_DETAIL_TABS
} from '../../../shared/constants/common.constant';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { HistoryService } from '../../../core/services/history/history.service';
import { SortOption } from '../../../shared/enums/sort-option.enum';
import { API_URI } from '../../../shared/constants/api.constant';
import { signal, ElementRef } from '@angular/core';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { afterEach, describe, beforeEach, expect, it } from 'vitest';


const products = MOCK_PRODUCTS._embedded.products;

describe('ProductDetailComponent', () => {
  let component: ProductDetailComponent;
  let fixture: ComponentFixture<ProductDetailComponent>;
  let routingQueryParamService: MockedObject<RoutingQueryParamService>;
  let languageService: MockedObject<LanguageService>;
  let titleService: Title;
  let sanitizerSpy: MockedObject<DomSanitizer>;
  let mockProductFeedbackService: MockedObject<ProductFeedbackService>;
  let mockProductStarRatingService: MockedObject<ProductStarRatingService>;
  let mockAuthService: MockedObject<AuthService>;
  let mockAppModalService: MockedObject<AppModalService>;
  let mockHistoryService: MockedObject<HistoryService>;
  let mockRouter: MockedObject<Router>;

  beforeEach(async () => {
    const spy = {
      bypassSecurityTrustHtml: vi
        .fn()
        .mockName('DomSanitizer.bypassSecurityTrustHtml'),
      sanitize: vi.fn().mockName('DomSanitizer.sanitize')
    };
    const routingQueryParamServiceSpy = {
      getDesignerVersionFromSessionStorage: vi
        .fn()
        .mockName(
          'RoutingQueryParamService.getDesignerVersionFromSessionStorage'
        ),
      isDesignerEnv: vi.fn().mockName('RoutingQueryParamService.isDesignerEnv')
    };

    const languageServiceSpy = {
      selectedLanguage: vi.fn().mockName('LanguageService.selectedLanguage')
    };

    mockHistoryService = {
      lastSearchType: vi.fn().mockName('HistoryService.lastSearchType'),
      lastSortOption: vi.fn().mockName('HistoryService.lastSortOption'),
      lastSearchText: vi.fn().mockName('HistoryService.lastSearchText'),
      isLastSearchChanged: vi
        .fn()
        .mockName('HistoryService.isLastSearchChanged')
    } as unknown as MockedObject<HistoryService>;

    mockProductFeedbackService = {
      getInitFeedbacksObservable: vi
        .fn()
        .mockName('ProductFeedbackService.getInitFeedbacksObservable'),
      findProductFeedbacksByCriteria: vi
        .fn()
        .mockName('ProductFeedbackService.findProductFeedbacksByCriteria'),
      handleFeedbackApiResponse: vi
        .fn()
        .mockName('ProductFeedbackService.handleFeedbackApiResponse'),
      findProductFeedbackOfUser: vi
        .fn()
        .mockName('ProductFeedbackService.findProductFeedbackOfUser'),
      feedbacks: signal<Feedback[]>([]),
      totalElements: signal(0)
    } as unknown as MockedObject<ProductFeedbackService>;

    mockRouter = {
      navigate: vi.fn().mockName('Router.navigate')
    } as unknown as MockedObject<Router>;

    mockAuthService = {
      getToken: vi.fn().mockName('AuthService.getToken'),
      redirectToGitHub: vi.fn().mockName('AuthService.redirectToGitHub')
    } as unknown as MockedObject<AuthService>;
    mockAppModalService = {
      openAddFeedbackDialog: vi
        .fn()
        .mockName('AppModalService.openAddFeedbackDialog')
    } as unknown as MockedObject<AppModalService>;
    mockProductStarRatingService = {
      getRatingObservable: vi
        .fn()
        .mockName('ProductStarRatingService.getRatingObservable'),
      starRatings: signal<StarRatingCounting[]>([]),
      totalComments: signal(0),
      reviewNumber: signal(0)
    } as unknown as MockedObject<ProductStarRatingService>;

    await TestBed.configureTestingModule({
      imports: [
        ProductDetailComponent,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        ProductStarRatingService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: ProductStarRatingService,
          useValue: mockProductStarRatingService
        },
        {
          provide: ProductFeedbackService,
          useValue: mockProductFeedbackService
        },
        { provide: AuthService, useValue: mockAuthService },
        { provide: AppModalService, useValue: mockAppModalService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: products[0].id },
              data: { productDetail: MOCK_PRODUCT_DETAIL },
              queryParamMap: convertToParamMap({
                version: MOCK_PRODUCT_DETAIL.productModuleContent.version
              })
            },
            queryParams: of({ type: TypeOption.CONNECTORS }),
            fragment: of('description')
          }
        },
        {
          provide: RoutingQueryParamService,
          useValue: routingQueryParamServiceSpy
        },
        { provide: Router, useValue: mockRouter },
        { provide: HistoryService, useValue: mockHistoryService },
        {
          provide: LanguageService,
          useValue: languageServiceSpy
        },
        Title,
        { provide: DomSanitizer, useValue: spy }
      ]
    })
      .overrideComponent(ProductDetailComponent, {
        remove: { providers: [ProductService] },
        add: {
          providers: [{ provide: ProductService, useClass: MockProductService }]
        }
      })
      .compileComponents();
    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as MockedObject<RoutingQueryParamService>;

    languageService = TestBed.inject(
      LanguageService
    ) as MockedObject<LanguageService>;

    sanitizerSpy = TestBed.inject(DomSanitizer) as MockedObject<DomSanitizer>;

    titleService = TestBed.inject(Title);
    mockProductFeedbackService.getInitFeedbacksObservable.mockReturnValue(
      of([MOCK_FEEDBACK_API_RESPONSE] as any as FeedbackApiResponse)
    );

    mockProductFeedbackService.findProductFeedbackOfUser.mockReturnValue(
      of({} as any as Feedback[])
    );
    mockAppModalService.openAddFeedbackDialog.mockReturnValue(
      Promise.resolve()
    );
    mockAuthService.getToken.mockReturnValue('token');
    mockProductStarRatingService.getRatingObservable.mockReturnValue(
      of([] as any as StarRatingCounting[])
    );
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductDetailComponent);
    component = fixture.componentInstance;
    const productService = TestBed.inject(ProductService);
    vi.spyOn(productService, 'setDefaultVendorImage');
    mockRouter.navigate.mockResolvedValue(true);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
    history.pushState(null, '', globalThis.location.pathname);
  });

  it('should create', () => {
    expect(component.productDetail().names['en']).toEqual(
      MOCK_PRODUCT_DETAIL.names['en']
    );
  });

  it('should open add feedback dialog if token is present', () => {
    languageService.selectedLanguage.mockReturnValue(Language.DE);
    mockAuthService.getToken.mockReturnValue('token');

    component.onClickRateBtn();
    fixture.detectChanges();

    expect(mockAppModalService.openAddFeedbackDialog).toHaveBeenCalled();
  });

  it('should redirect to Gitub if token is null', () => {
    languageService.selectedLanguage.mockReturnValue(Language.DE);
    mockAuthService.getToken.mockReturnValue(null);

    component.onClickRateBtn();
    fixture.detectChanges();

    expect(mockAuthService.redirectToGitHub).toHaveBeenCalled();
  });

  it('should have title like the name DE', () => {
    languageService.selectedLanguage.mockReturnValue(Language.DE);
    component.updateWebBrowserTitle(component.productDetail().names);
    fixture.detectChanges();

    expect(titleService.getTitle()).toEqual(
      MOCK_PRODUCT_DETAIL.names[Language.DE]
    );
  });

  it('version should display in number', () => {
    expect(component.selectedVersion).toEqual('Version 10.0.0');
  });

  it('should reset state before fetching new product details', () => {
    component.productDetail.set(MOCK_PRODUCT_DETAIL);
    component.productModuleContent.set(
      MOCK_PRODUCT_DETAIL.productModuleContent
    );

    expect(component.productDetail().id).toBe('jira-connector');
    expect(component.productModuleContent().name).toBe('Jira Connector');
  });

  it('should update dropdown selection on updateDropdownSelection', () => {
    document.body.innerHTML = `<select id="nav_item"><option value="description">Description</option></select>`;
    component.activeTab = 'description';
    component.updateDropdownSelection();
    const dropdown = document.getElementById('nav_item') as HTMLSelectElement;
    expect(dropdown.value).toBe('description');
  });

  it('should call setActiveTab and updateDropdownSelection on setActiveTab', () => {
    const event = { value: 'description' };
    vi.spyOn(component, 'setActiveTab');
    vi.spyOn(component, 'updateDropdownSelection');

    component.setActiveTab(event.value);

    expect(component.setActiveTab).toHaveBeenCalledWith('description');
  });

  it('should not display information when product detail is empty', () => {
    const mockContentWithEmptySetup: ProductModuleContent =
      {} as ProductModuleContent;
    component.productModuleContent.set(mockContentWithEmptySetup);
    expect(component.isEmptyProductContent()).toBe(true);
    fixture.detectChanges();
    const description = fixture.debugElement.query(By.css('#description'));
    expect(description).toBeFalsy();
  });

  it('should return true for description when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(true);
  });

  it('should return true for description when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { de: 'Test description' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(true);
  });

  it('should return true for description when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(true);
  });

  it('should return true for description when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(true);
  });

  it('should return false for description when it is null', () => {
    const mockContentWithNullDescription: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullDescription);
    expect(component.getContent('description')).toBe(false);
  });

  it('should return false for any tab when detail content is undefined or null', () => {
    component.productModuleContent.set(null as any as ProductModuleContent);
    expect(component.getContent('description')).toBe(false);
    component.productModuleContent.set(
      undefined as any as ProductModuleContent
    );
    expect(component.getContent('description')).toBe(false);
    component.productModuleContent.set({} as any as ProductModuleContent);
    expect(component.getContent('description')).toBe(false);
  });

  it('should return false for description when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: '', de: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(false);
  });

  it('should return false for description when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { de: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(false);
  });

  it('should return false for description when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(false);
  });

  it('should return false for description when in both DE and EN language it is an undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBe(false);
  });

  it('should return true for setup when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(true);
  });

  it('should return true for setup when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { de: 'Test setup' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(true);
  });

  it('should return true for setup when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(true);
  });

  it('should return true for setup when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(true);
  });

  it('should return false for setup when it is null', () => {
    const mockContentWithNullSetup: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullSetup);
    expect(component.getContent('setup')).toBe(false);
  });

  it('should return false for setup when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: '', de: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(false);
  });

  it('should return false for setup when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { de: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(false);
  });

  it('should return false for setup when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(false);
  });

  it('should return false for setup when in both DE and EN language it is an undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBe(false);
  });

  it('should return true for demo when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(true);
  });

  it('should return true for demo when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { de: 'Test demo' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(true);
  });

  it('should return true for demo when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(true);
  });

  it('should return true for demo when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(true);
  });

  it('should return false for demo when it is null', () => {
    const mockContentWithNullDemo: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullDemo);
    expect(component.getContent('demo')).toBe(false);
  });

  it('should return false for demo when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: '', de: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(false);
  });

  it('should return false for demo when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { de: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(false);
  });

  it('should return false for demo when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(false);
  });

  it('should return false for demo when in both DE and EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.mockReturnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBe(false);
  });

  it('should return false for changelog when productReleaseSafeHtmls is empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };

    component.productReleaseSafeHtmls = signal([]);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('changelog')).toBe(false);
  });

  it('should display dropdown horizontally on small viewport', () => {
    const dropdown = fixture.debugElement.query(By.css('.dropdown-tab'));

    // Bootstrap flex-row class drives horizontal layout (jsdom can't compute CSS)
    expect(dropdown).toBeTruthy();
    expect(dropdown.nativeElement.classList.contains('flex-row')).toBe(true);
  });

  it('should display dropdown instead of tabs when viewport width is 540px', () => {
    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const tabs = tabGroup.query(By.css('.row-tab d-none d-xl-block col-12'));
    const dropdown = tabGroup.query(By.css('.dropdown-tab'));

    expect(tabs).toBeFalsy();
    expect(dropdown).toBeTruthy();
  });

  it('should display tabs instead of dropdown when viewport width is above 540px', () => {
    Object.defineProperty(globalThis, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1920
    });
    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const dropdown = tabGroup.query(
      By.css(
        '.dropdown-tab d-block d-xl-none d-flex flex-row justify-content-center align-items-center w-100'
      )
    );

    expect(dropdown).toBeFalsy();
  });

  it('should display info tab on click of info icon for smaller screens', () => {
    Object.defineProperty(globalThis, 'innerWidth', { writable: true, configurable: true, value: 540 });

    let infoTab = fixture.debugElement.query(
      By.css(
        '.info-tab d-none d-xl-block d-flex flex-column flex-grow-1 align-items-start col-xl-3'
      )
    );
    expect(infoTab).toBeFalsy();

    const infoIcon = fixture.debugElement.query(By.css('.info-icon'));
    infoIcon.triggerEventHandler('click', null);
    fixture.detectChanges();

    infoTab = fixture.debugElement.query(By.css('.info-tab'));
    expect(infoTab).toBeTruthy();
  });

  it('should call checkMediaSize on ngOnInit', async () => {
    vi.spyOn(component, 'checkMediaSize');
    component.ngOnInit();
    await Promise.resolve();
    expect(component.checkMediaSize).toHaveBeenCalled();
  });

  it('should set isMobileMode based on window size', () => {
    vi.spyOn(globalThis, 'matchMedia').mockReturnValue({
      matches: true,
      media: '',
      addEventListener: () => {},
      removeEventListener: () => {},
      onchange: null,
      addListener: function (
        callback:
          | ((this: MediaQueryList, ev: MediaQueryListEvent) => any)
          | null
      ): void {
        throw new Error('Function not implemented.');
      },
      removeListener: function (
        callback:
          | ((this: MediaQueryList, ev: MediaQueryListEvent) => any)
          | null
      ): void {
        throw new Error('Function not implemented.');
      },
      dispatchEvent: function (event: Event): boolean {
        throw new Error('Function not implemented.');
      }
    });

    component.checkMediaSize();
    expect(component.isMobileMode()).toBe(true);

    (globalThis.matchMedia as Mock).mockReturnValue({
      matches: false,
      media: '',
      addEventListener: () => {},
      removeEventListener: () => {}
    });

    component.checkMediaSize();
    expect(component.isMobileMode()).toBe(false);
  });

  it('should call checkMediaSize on window resize', () => {
    vi.spyOn(component, 'checkMediaSize');
    component.onResize();
    expect(component.checkMediaSize).toHaveBeenCalled();
  });

  it('should be empty selected version if product detail content is missing', () => {
    component.productModuleContent.set({} as ProductModuleContent);
    component.selectedVersion = '';
    component.handleProductContentVersion();
    expect(component.selectedVersion).toEqual('');
  });

  it('should be formated selected version if open in designer', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      version: '10.0.11'
    };
    component.productModuleContent.set(mockContent);
    routingQueryParamService.isDesignerEnv.mockReturnValue(true);
    component.handleProductContentVersion();
    expect(component.selectedVersion).toEqual('Version 10.0.11');
  });

  it('should return DESIGNER_ENV as action type in Designer Env', () => {
    routingQueryParamService.isDesignerEnv.mockReturnValue(true);

    component.updateProductDetailActionType({ sourceUrl: 'some-url' } as any);
    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.DESIGNER_ENV
    );
  });

  it('should call handleProductDetailOnInit when version is empty', () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.snapshot.queryParamMap = convertToParamMap({ version: '' });

    fixture = TestBed.createComponent(ProductDetailComponent);
    component = fixture.componentInstance;

    const spy = vi.spyOn(component, 'handleProductDetailOnInit');

    fixture.detectChanges();

    expect(spy).toHaveBeenCalled();
  });

  it('should return CUSTOM_SOLUTION as action type when productDetail.sourceUrl is undefined', () => {
    routingQueryParamService.isDesignerEnv.mockReturnValue(false);

    component.updateProductDetailActionType({ sourceUrl: undefined } as any);

    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.CUSTOM_SOLUTION
    );
    fixture.detectChanges();
    let installationCount = fixture.debugElement.query(
      By.css('#app-product-installation-count-action')
    );
    expect(installationCount).toBeFalsy();
  });

  it('should return STANDARD as action type when when productDetail.sourceUrl is defined', () => {
    routingQueryParamService.isDesignerEnv.mockReturnValue(false);

    component.updateProductDetailActionType({ sourceUrl: 'some-url' } as any);

    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.STANDARD
    );
  });

  it('displayed tabs array should have size 0 if product module content description, setup, demo, dependency, and changelog are all empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };
    component.productModuleContent.set(mockContent);
    component.productReleaseSafeHtmls = signal([]);

    expect(component.displayedTabsSignal().length).toBe(0);
  });

  it('should hide tab and tab content when all tabs have no content', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };
    component.productModuleContent.set(mockContent);

    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const tabs = tabGroup.query(By.css('.row-tab d-none d-xl-block col-12'));
    const dropdown = tabGroup.query(
      By.css(
        '.dropdown-tab d-block d-xl-none d-flex flex-row justify-content-center align-items-center w-100'
      )
    );
    const tabContent = tabGroup.query(
      By.css('.tab-content col-12 default-cursor')
    );

    expect(tabs).toBeFalsy();
    expect(dropdown).toBeFalsy();
    expect(tabContent).toBeFalsy();
  });

  it('should generate right text for the rate connector', () => {
    let rateConnector = fixture.debugElement.query(
      By.css('.rate-connector-btn')
    );
    expect(rateConnector.childNodes[0].nativeNode.textContent).toContain(
      'common.feedback.rateFeedbackForConnectorBtnLabel'
    );

    let rateConnectorEmptyText = fixture.debugElement.query(
      By.css('.rate-empty-text')
    );
    expect(
      rateConnectorEmptyText.childNodes[0].nativeNode.textContent
    ).toContain('common.feedback.noFeedbackForConnectorLabel');

    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.snapshot.data.productDetail = MOCK_CRON_JOB_PRODUCT_DETAIL;

    // Recreate the component with new data
    fixture = TestBed.createComponent(ProductDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Now assert utility labels
    rateConnector = fixture.debugElement.query(By.css('.rate-connector-btn'));
    expect(rateConnector.childNodes[0].nativeNode.textContent.trim()).toContain(
      'common.feedback.rateFeedbackForUtilityBtnLabel'
    );

    rateConnectorEmptyText = fixture.debugElement.query(
      By.css('.rate-empty-text')
    );
    expect(
      rateConnectorEmptyText.childNodes[0].nativeNode.textContent
    ).toContain('common.feedback.noFeedbackForUtilityLabel');
  });

  it('maven tab should not display when product module content is missing', () => {
    const event = { value: 'dependency' };
    component.setActiveTab(event.value);
    fixture.detectChanges();
    let mavenTab = fixture.debugElement.query(
      By.css('app-product-detail-maven-content')
    );

    expect(mavenTab).toBeTruthy();
    component.productModuleContent.set({} as any as ProductModuleContent);
    fixture.detectChanges();
    mavenTab = fixture.debugElement.query(
      By.css('app-product-detail-maven-content')
    );
    expect(mavenTab).toBeFalsy();
  });

  it('should render GitHub alert as safe HTML', () => {
    const value = '**This is a test**';
    const mockedRenderedHtml = '<strong>This is a test</strong>';
    sanitizerSpy.bypassSecurityTrustHtml.mockReturnValue(mockedRenderedHtml);

    const result = component.renderGithubAlert(value);

    expect(result).toBe(mockedRenderedHtml);
  });

  it('should process README content correctly with different values per tab', () => {
    languageService.selectedLanguage.mockReturnValue(Language.EN);

    vi.spyOn(component, 'getProductModuleContentValue').mockImplementation(
      tab => {
        const key = tab.value as keyof ProductModuleContent;
        return MOCK_PRODUCT_DETAIL.productModuleContent[key] as {
          en: string;
        };
      }
    );

    vi.spyOn(MultilingualismPipe.prototype, 'transform').mockImplementation(
      content => JSON.stringify(content)
    );
    vi.spyOn(component, 'renderGithubAlert').mockImplementation(
      (content: string) => `${content}` as SafeHtml
    );

    component.getReadmeContent();

    expect(component.loadedReadmeContent['description']).toBe(
      JSON.stringify(MOCK_PRODUCT_DETAIL.productModuleContent.description)
    );
    expect(component.loadedReadmeContent['demo']).toBe(
      JSON.stringify(MOCK_PRODUCT_DETAIL.productModuleContent.demo)
    );

    expect(component.getProductModuleContentValue).toHaveBeenCalled();
    expect(MultilingualismPipe.prototype.transform).toHaveBeenCalled();
    expect(component.renderGithubAlert).toHaveBeenCalled();
  });

  it('should not process content if getProductModuleContentValue returns null', () => {
    vi.spyOn(component, 'getProductModuleContentValue').mockReturnValue(null);
    vi.spyOn(component, 'renderGithubAlert');

    component.getReadmeContent();

    expect(component.getProductModuleContentValue).toHaveBeenCalledTimes(
      PRODUCT_DETAIL_TABS.length
    );
    expect(component.renderGithubAlert).not.toHaveBeenCalled();
  });

  it('should close the dropdown when clicking outside', async () => {
    component.isDropdownOpen.set(true);
    fixture.detectChanges();
    await fixture.whenStable();

    const outsideElement = document.createElement('div');
    document.body.appendChild(outsideElement);
    outsideElement.click();

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.isDropdownOpen()).toBe(false);

    outsideElement.remove();
  });

  it('should replace GitHub URLs with appropriate links in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';
    component.linkifyPullRequests(
      md,
      sourceUrl,
      GITHUB_PULL_REQUEST_NUMBER_REGEX
    );

    const inputText =
      'Check out this PR: https://github.com/source-repo/pull/123';
    const expectedOutput = 'Check out this PR: #123';
    const result = md.renderInline(inputText);

    expect(result).toContain(expectedOutput);
  });

  it('should keep GitHub URLs if they contain compare string in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';
    component.linkifyPullRequests(
      md,
      sourceUrl,
      GITHUB_PULL_REQUEST_NUMBER_REGEX
    );

    const inputText =
      'Check out this PR: https://github.com/source-repo/compare/123';
    const expectedOutput =
      'Check out this PR: https://github.com/source-repo/compare/123';
    const result = md.renderInline(inputText);

    expect(result).toContain(expectedOutput);
  });

  it('should render changelog content with safe HTML', () => {
    const mockReleases = [
      {
        name: '1.0.0',
        body: 'Initial release',
        publishedAt: '2023-01-01',
        htmlUrl: 'https://github.com/axonivy-market/portal/releases/tag/1.0.0',
        latestRelease: true
      }
    ];
    const expectedSafeHtml = '<p>Initial release</p>';
    sanitizerSpy.bypassSecurityTrustHtml.mockReturnValue(expectedSafeHtml);

    const result = component.renderChangelogContent(mockReleases);

    expect(result[0].body).toBe(expectedSafeHtml);
  });

  it('should navigate with correct query parameters when last recorded values differ from defaults', () => {
    mockHistoryService.lastSearchType.mockReturnValue(TypeOption.CONNECTORS);
    mockHistoryService.lastSortOption.mockReturnValue(SortOption.POPULARITY);
    mockHistoryService.lastSearchText.mockReturnValue('test-search');

    component.navigateToHomePageWithLastSearch();

    expect(mockRouter.navigate).toHaveBeenCalledWith([API_URI.APP], {
      relativeTo: expect.anything(),
      queryParamsHandling: 'merge',
      queryParams: {
        type: TypeOption.CONNECTORS,
        sort: SortOption.POPULARITY,
        search: 'test-search'
      }
    });
  });

  it('should navigate with empty query params when defaults are present', () => {
    mockHistoryService.lastSearchType.mockReturnValue(TypeOption.All_TYPES);
    mockHistoryService.lastSortOption.mockReturnValue(SortOption.STANDARD);
    mockHistoryService.lastSearchText.mockReturnValue('');

    component.navigateToHomePageWithLastSearch();

    expect(mockRouter.navigate).toHaveBeenCalledWith([API_URI.APP], {
      relativeTo: expect.anything(),
      queryParamsHandling: 'merge',
      queryParams: {}
    });
  });

  it('should navigate to home page when click back to button regardless history', () => {
    mockHistoryService.lastSearchType.mockReturnValue(TypeOption.All_TYPES);
    mockHistoryService.lastSortOption.mockReturnValue(SortOption.STANDARD);
    mockHistoryService.lastSearchText.mockReturnValue('');
    component.setActiveTab('description');
    component.setActiveTab('demo');
    component.setActiveTab('setup');
    component.onClickingBackToHomepageButton();
    expect(mockRouter.navigate).toHaveBeenCalledWith([API_URI.APP]);
  });

  it('should call setActiveTab with correct tab value from fragment', () => {
    const spy = vi.spyOn(component, 'setActiveTab');
    component.navigateToProductDetailsWithTabFragment();
    expect(spy).toHaveBeenCalled();
    const args = spy.mock.lastCall!;
    expect(args[0]).toBe('description');
    expect(args[2]).toBe(false);
  });

  describe('loadChangelogs', () => {
    let productService: MockedObject<ProductService>;

    beforeEach(() => {
      productService = {
        getProductChangelogs: vi
          .fn()
          .mockName('ProductService.getProductChangelogs')
      } as unknown as MockedObject<ProductService>;
    });

    it('should load and append new changelogs successfully', () => {
      // Arrange
      const mockResponse = {
        _embedded: {
          gitHubReleaseModelList: [
            {
              name: '12.0.4',
              body: '## New Features\r\n- Feature A\r\n- Feature B',
              publishedAt: '2025-01-21T10:19:19.000+00:00',
              htmlUrl:
                'https://github.com/axonivy-market/portal/releases/tag/12.0.4',
              latestRelease: false
            }
          ]
        },
        _links: {
          self: {
            href: 'http://localhost:8080/api/product-details/portal/releases?page=1&size=20'
          },
          next: {
            href: 'http://localhost:8080/api/product-details/portal/releases?page=2&size=20'
          }
        },
        page: {
          size: 20,
          totalElements: 2,
          totalPages: 2,
          number: 1
        }
      };

      const existingChangelogs = [
        {
          name: '12.0.3',
          body: sanitizerSpy.bypassSecurityTrustHtml('<p>Previous release</p>'),
          publishedAt: '2025-01-20T10:19:19.000+00:00',
          htmlUrl:
            'https://github.com/axonivy-market/portal/releases/tag/12.0.3',
          isLatestRelease: true
        }
      ];

      component.productReleaseSafeHtmls.set(existingChangelogs);
      component.productService = productService;
      productService.getProductChangelogs.mockReturnValue(
        of(mockResponse as any)
      );
      vi.spyOn(component, 'renderChangelogContent').mockReturnValue([
        {
          name: '12.0.4',
          body: sanitizerSpy.bypassSecurityTrustHtml(
            '<p>New release content</p>'
          ),
          publishedAt: '2025-01-21T10:19:19.000+00:00',
          htmlUrl:
            'https://github.com/axonivy-market/portal/releases/tag/12.0.4',
          isLatestRelease: false
        }
      ]);

      // Act
      component.loadChangelogs();

      // Assert
      expect(productService.getProductChangelogs).toHaveBeenCalledWith(
        component.criteria
      );
      expect(component.renderChangelogContent).toHaveBeenCalledWith(
        mockResponse._embedded.gitHubReleaseModelList
      );
      expect(component.productReleaseSafeHtmls().length).toBe(2);
      expect(component.changeLogLinks).toEqual(mockResponse._links);
      expect(component.changeLogPages).toEqual(mockResponse.page);
      expect(component.criteria.nextPageHref).toBe(
        mockResponse._links.next.href
      );
    });

    it('should handle empty response', () => {
      // Arrange
      component.productService = productService;
      productService.getProductChangelogs.mockReturnValue(of(null as any));

      const initialChangelogs = component.productReleaseSafeHtmls();

      // Act
      component.loadChangelogs();

      // Assert
      expect(productService.getProductChangelogs).toHaveBeenCalledWith(
        component.criteria
      );
      expect(component.productReleaseSafeHtmls()).toEqual(initialChangelogs);
    });

    it('should handle response with empty changelog list', () => {
      // Arrange
      const mockResponse = {
        _embedded: {
          gitHubReleaseModelList: []
        },
        _links: {
          self: {
            href: 'http://localhost:8080/api/product-details/portal/releases?page=0&size=20'
          }
        },
        page: {
          size: 20,
          totalElements: 0,
          totalPages: 0,
          number: 0
        }
      };

      component.productService = productService;
      productService.getProductChangelogs.mockReturnValue(
        of(mockResponse as any)
      );

      const initialChangelogs = component.productReleaseSafeHtmls();

      // Act
      component.loadChangelogs();

      // Assert
      expect(productService.getProductChangelogs).toHaveBeenCalledWith(
        component.criteria
      );
      expect(component.productReleaseSafeHtmls()).toEqual(initialChangelogs);
      expect(component.changeLogLinks).toEqual(mockResponse._links);
      expect(component.changeLogPages).toEqual(mockResponse.page);
      expect(component.criteria.nextPageHref).toBeUndefined();
    });

    it('should handle response with missing _embedded property', () => {
      // Arrange
      const mockResponse = {
        _links: {
          self: {
            href: 'http://localhost:8080/api/product-details/portal/releases?page=0&size=20'
          }
        },
        page: {
          size: 20,
          totalElements: 0,
          totalPages: 0,
          number: 0
        }
      };

      component.productService = productService;
      productService.getProductChangelogs.mockReturnValue(
        of(mockResponse as any)
      );

      const initialChangelogs = component.productReleaseSafeHtmls();

      // Act
      component.loadChangelogs();

      // Assert
      expect(productService.getProductChangelogs).toHaveBeenCalledWith(
        component.criteria
      );
      expect(component.productReleaseSafeHtmls()).toEqual(initialChangelogs);
      expect(component.changeLogLinks).toEqual(mockResponse._links);
      expect(component.changeLogPages).toEqual(mockResponse.page);
      expect(component.criteria.nextPageHref).toBeUndefined();
    });

    it('should handle error response', () => {
      // Arrange
      const errorResponse = new Error('Network error');
      component.productService = productService;
      productService.getProductChangelogs.mockReturnValue(
        new Observable((subscriber: any) => {
          subscriber.error(errorResponse);
        })
      );

      vi.spyOn(console, 'error'); // Prevent error logging in test output

      // Act & Assert
      expect(() => component.loadChangelogs()).not.toThrow();
      expect(productService.getProductChangelogs).toHaveBeenCalledWith(
        component.criteria
      );
    });
  });

  describe('setupIntersectionObserver', () => {
    let mockObserver: MockedObject<IntersectionObserver>;
    let mockObserverElement: MockedObject<ElementRef>;
    let originalIntersectionObserver: any;
    let lastIOCallback: IntersectionObserverCallback | undefined;

    beforeEach(() => {
      mockObserver = {
        observe: vi.fn().mockName('IntersectionObserver.observe')
      } as unknown as MockedObject<IntersectionObserver>;
      mockObserverElement = {
        nativeElement: document.createElement('div')
      };
      lastIOCallback = undefined;

      // Store original IntersectionObserver and mock it
      originalIntersectionObserver = (globalThis as any).IntersectionObserver;
      const ioSpy = vi.fn().mockImplementation(function (
        callback: IntersectionObserverCallback,
        _options?: IntersectionObserverInit
      ) {
        // Store the callback for testing
        lastIOCallback = callback;
        return mockObserver;
      });
      (globalThis as any).IntersectionObserver = ioSpy;
    });

    afterEach(() => {
      // Restore original IntersectionObserver
      (globalThis as any).IntersectionObserver = originalIntersectionObserver;
    });

    it('should return early if observerElement is not available', () => {
      // Arrange
      component.observerElement = undefined as any;
      component.isBrowser = true;

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((globalThis as any).IntersectionObserver).not.toHaveBeenCalled();
    });

    it('should return early if changelogIntersectionObserver already exists', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      (component as any).changelogIntersectionObserver = mockObserver;
      component.isBrowser = true;

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((globalThis as any).IntersectionObserver).not.toHaveBeenCalled();
    });

    it('should return early if not in browser environment', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = false;

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((globalThis as any).IntersectionObserver).not.toHaveBeenCalled();
    });

    it('should return early if IntersectionObserver is not supported', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (globalThis as any).IntersectionObserver = undefined;

      // Act & Assert - In this case we can't check if it was called since it's undefined
      // The test passes if no error is thrown
      expect(() => component.setupIntersectionObserver()).not.toThrow();
    });

    it('should create IntersectionObserver with correct options and observe element', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;
      ((globalThis as any).IntersectionObserver as Mock).mockImplementation(
        function () { return mockObserver; }
      );

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((globalThis as any).IntersectionObserver).toHaveBeenCalledWith(
        expect.any(Function),
        { root: null, rootMargin: '10px', threshold: 0.1 }
      );
      expect(mockObserver.observe).toHaveBeenCalledWith(
        mockObserverElement.nativeElement
      );
    });

    it('should call loadChangelogs when entry is intersecting and has more changelogs', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      vi.spyOn(component, 'hasMoreChangelogs').mockReturnValue(true);
      vi.spyOn(component, 'loadChangelogs').mockImplementation(() => {});

      component.changeLogLinks = { next: { href: 'next-page-url' } } as any;

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = lastIOCallback;

      // Simulate intersection
      const mockEntries: IntersectionObserverEntry[] = [
        {
          isIntersecting: true,
          target: mockObserverElement.nativeElement,
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0.5,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        }
      ];

      expect(observerCallback).toBeDefined();
      observerCallback!(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).toHaveBeenCalled();
      expect(component.criteria.nextPageHref).toBe('next-page-url');
      expect(component.loadChangelogs).toHaveBeenCalled();
    });

    it('should not call loadChangelogs when entry is intersecting but has no more changelogs', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      vi.spyOn(component, 'hasMoreChangelogs').mockReturnValue(false);
      vi.spyOn(component, 'loadChangelogs');

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = lastIOCallback;

      // Simulate intersection
      const mockEntries: IntersectionObserverEntry[] = [
        {
          isIntersecting: true,
          target: mockObserverElement.nativeElement,
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0.5,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        }
      ];

      expect(observerCallback).toBeDefined();
      observerCallback!(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).toHaveBeenCalled();
      expect(component.loadChangelogs).not.toHaveBeenCalled();
    });

    it('should not call loadChangelogs when entry is not intersecting', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      vi.spyOn(component, 'hasMoreChangelogs').mockReturnValue(true);
      vi.spyOn(component, 'loadChangelogs');

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = lastIOCallback;

      // Simulate non-intersection
      const mockEntries: IntersectionObserverEntry[] = [
        {
          isIntersecting: false,
          target: mockObserverElement.nativeElement,
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        }
      ];

      expect(observerCallback).toBeDefined();
      observerCallback!(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).not.toHaveBeenCalled();
      expect(component.loadChangelogs).not.toHaveBeenCalled();
    });

    it('should not call loadChangelogs when isLoading', () => {
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      component.loadingService.showLoading(
        LoadingComponentId.PRODUCT_CHANGELOG
      );
      (component as any).changelogIntersectionObserver = undefined;
      vi.spyOn(component, 'hasMoreChangelogs').mockReturnValue(true);
      vi.spyOn(component, 'loadChangelogs');

      component.setupIntersectionObserver();
      const observerCallback = lastIOCallback;
      const mockEntries: IntersectionObserverEntry[] = [
        {
          isIntersecting: false,
          target: mockObserverElement.nativeElement,
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        }
      ];
      expect(observerCallback).toBeDefined();
      observerCallback!(mockEntries, mockObserver);
      expect(component.loadChangelogs).not.toHaveBeenCalled();
    });

    it('should handle multiple entries in intersection callback', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      vi.spyOn(component, 'hasMoreChangelogs').mockReturnValue(true);
      vi.spyOn(component, 'loadChangelogs');

      component.changeLogLinks = { next: { href: 'next-page-url' } } as any;

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = lastIOCallback;

      // Simulate multiple entries with both intersecting (to trigger hasMoreChangelogs twice)
      const mockEntries: IntersectionObserverEntry[] = [
        {
          isIntersecting: true,
          target: document.createElement('div'),
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0.3,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        },
        {
          isIntersecting: true,
          target: mockObserverElement.nativeElement,
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0.5,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        }
      ];

      expect(observerCallback).toBeDefined();
      observerCallback!(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).toHaveBeenCalledTimes(2);
      expect(component.loadChangelogs).toHaveBeenCalledTimes(2);
    });

    it('should set nextPageHref correctly when intersection occurs', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      vi.spyOn(component, 'hasMoreChangelogs').mockReturnValue(true);
      vi.spyOn(component, 'loadChangelogs').mockImplementation(() => {});

      const expectedHref = 'http://example.com/next-page';
      component.changeLogLinks = { next: { href: expectedHref } } as any;
      component.criteria.nextPageHref = 'initial-href';

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = lastIOCallback;

      // Simulate intersection
      const mockEntries: IntersectionObserverEntry[] = [
        {
          isIntersecting: true,
          target: mockObserverElement.nativeElement,
          boundingClientRect: {} as DOMRectReadOnly,
          intersectionRatio: 0.5,
          intersectionRect: {} as DOMRectReadOnly,
          rootBounds: {} as DOMRectReadOnly,
          time: Date.now()
        }
      ];

      expect(observerCallback).toBeDefined();
      observerCallback!(mockEntries, mockObserver);

      // Assert
      expect(component.criteria.nextPageHref).toBe(expectedHref);
      expect(component.loadChangelogs).toHaveBeenCalled();
    });
  });

  // Test onPopState
  it('should handle popstate event and update activeTab', () => {
    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;
    component.activeTab = 'description';
    component.productDetail.set(MOCK_PRODUCT_DETAIL);
    history.pushState(null, '', '#demo');

    component.onPopState();

    expect(component.activeTab).toBe('demo');
  });

  it('should not update activeTab if tabValue equals activeTab on popstate', () => {
    component.activeTab = 'description';

    history.pushState(null, '', '#description');

    component.onPopState();

    expect(component.activeTab).toBe('description');
  });

  it('should update activeTab on popstate', () => {
    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;
    component.activeTab = 'description';

    history.pushState(null, '', '#demo');

    component.onPopState();

    expect(component.activeTab).toBe('demo');
  });

  // Test handleFirstTabActivation
  it('should navigate with DEFAULT_ACTIVE_TAB when tab is empty in handleFirstTabActivation', () => {
    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = false;
    component.productDetail.set(MOCK_PRODUCT_DETAIL);

    component.setActiveTab('');

    expect(mockRouter.navigate).toHaveBeenCalledWith(
      [],
      expect.objectContaining({
        fragment: 'description' // DEFAULT_ACTIVE_TAB
      })
    );
  });

  it('should set DEFAULT_ACTIVE_TAB when fragment is null and not initialFragmentHandled', async () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.fragment = of(null);

    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = false;
    component.productDetail.set(MOCK_PRODUCT_DETAIL);

    const spy = vi.spyOn(component, 'setActiveTab');

    component.navigateToProductDetailsWithTabFragment();
    await Promise.resolve();

    expect(spy).not.toHaveBeenCalled();
    expect(component.activeTab).toBe('description');
    expect((component as any).initialFragmentHandled).toBe(true);
  });

  // Test handleSubsequentTabActivation - changelog
  it('should call setupIntersectionObserver when tab is changelog in handleSubsequentTabActivation', () => {
    vi.useFakeTimers();
    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;
    component.productDetail.set(MOCK_PRODUCT_DETAIL);

    vi.spyOn(component, 'setupIntersectionObserver');

    component.setActiveTab('changelog');
    vi.runAllTimers();

    expect(component.setupIntersectionObserver).toHaveBeenCalled();
  });

  it('should NOT navigate when fragment is null and initialFragmentHandled is true', async () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.fragment = of(null);

    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;
    component.activeTab = 'demo';

    component.navigateToProductDetailsWithTabFragment();
    await Promise.resolve();

    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should not navigate if currentFragment already equals currentTab', async () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.fragment = of(null);
    activatedRoute.snapshot.fragment = 'demo';

    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;
    component.activeTab = 'demo';

    mockRouter.navigate.mockClear();

    component.navigateToProductDetailsWithTabFragment();
    await Promise.resolve();

    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should clear scrollTimeout on ngOnDestroy', () => {
    (component as any).scrollTimeout = setTimeout(() => { }, 1000);
    const clearSpy = vi.spyOn(window, 'clearTimeout');
    component.ngOnDestroy();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should call setActiveTab with scrollToTab false for description fragment', () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.fragment = of('description');

    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;

    const spy = vi.spyOn(component, 'setActiveTab');

    component.navigateToProductDetailsWithTabFragment();

    expect(spy).toHaveBeenCalledWith('description', true, false);
  });

  it('should call setActiveTab with scrollToTab true for non-default fragment', () => {
    const activatedRoute = TestBed.inject(ActivatedRoute) as any;
    activatedRoute.fragment = of('demo');

    (component as any).isDataLoaded = true;
    (component as any).initialFragmentHandled = true;

    const spy = vi.spyOn(component, 'setActiveTab');

    component.navigateToProductDetailsWithTabFragment();

    expect(spy).toHaveBeenCalledWith('demo', true, true);
  });

  describe('getDeprecationSuccessorName', () => {
    it('should return empty string when successor is empty string', () => {
      component.productDetail.set({ ...MOCK_PRODUCT_DETAIL, successor: '' });
      expect(component.getDeprecationSuccessorName()).toBe('');
    });

    it('should return last path segment for a valid URL', () => {
      component.productDetail.set({
        ...MOCK_PRODUCT_DETAIL,
        successor: 'https://market.axonivy.com/portal'
      });
      expect(component.getDeprecationSuccessorName()).toBe('portal');
    });

    it('should return decoded last path segment for an encoded URL', () => {
      component.productDetail.set({
        ...MOCK_PRODUCT_DETAIL,
        successor: 'https://market.axonivy.com/my%20connector'
      });
      expect(component.getDeprecationSuccessorName()).toBe('my connector');
    });

    it('should return hostname when URL has no path segments', () => {
      component.productDetail.set({
        ...MOCK_PRODUCT_DETAIL,
        successor: 'https://market.axonivy.com'
      });
      expect(component.getDeprecationSuccessorName()).toBe('market.axonivy.com');
    });

    it('should handle URL with trailing slash', () => {
      component.productDetail.set({
        ...MOCK_PRODUCT_DETAIL,
        successor: 'https://market.axonivy.com/portal/'
      });
      expect(component.getDeprecationSuccessorName()).toBe('portal');
    });
  });
});
