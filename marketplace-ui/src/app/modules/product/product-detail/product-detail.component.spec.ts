import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { By, DomSanitizer, SafeHtml, Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
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

const products = MOCK_PRODUCTS._embedded.products;
declare const viewport: Viewport;

describe('ProductDetailComponent', () => {
  let component: ProductDetailComponent;
  let fixture: ComponentFixture<ProductDetailComponent>;
  let routingQueryParamService: jasmine.SpyObj<RoutingQueryParamService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let titleService: Title;
  let sanitizerSpy: jasmine.SpyObj<DomSanitizer>;
  let mockProductFeedbackService: jasmine.SpyObj<ProductFeedbackService>;
  let mockProductStarRatingService: jasmine.SpyObj<ProductStarRatingService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockAppModalService: jasmine.SpyObj<AppModalService>;
  let mockHistoryService: jasmine.SpyObj<HistoryService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('DomSanitizer', [
      'bypassSecurityTrustHtml',
      'sanitize'
    ]);
    const routingQueryParamServiceSpy = jasmine.createSpyObj(
      'RoutingQueryParamService',
      ['getDesignerVersionFromSessionStorage', 'isDesignerEnv']
    );

    const languageServiceSpy = jasmine.createSpyObj('LanguageService', [
      'selectedLanguage'
    ]);

    mockHistoryService = jasmine.createSpyObj('HistoryService', [
      'lastSearchType',
      'lastSortOption',
      'lastSearchText',
      'isLastSearchChanged'
    ]);

    mockProductFeedbackService = jasmine.createSpyObj(
      'ProductFeedbackService',
      [
        'getInitFeedbacksObservable',
        'findProductFeedbacksByCriteria',
        'handleFeedbackApiResponse',
        'findProductFeedbackOfUser',
        'totalElements'
      ],
      {
        feedbacks: signal([]),
        totalElements: signal(0)
      }
    );

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockAuthService = jasmine.createSpyObj('AuthService', [
      'getToken',
      'redirectToGitHub'
    ]);
    mockAppModalService = jasmine.createSpyObj('AppModalService', [
      'openAddFeedbackDialog'
    ]);
    mockProductStarRatingService = jasmine.createSpyObj(
      'ProductStarRatingService',
      ['getRatingObservable', 'starRatings', 'totalComments', 'reviewNumber']
    );

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
              data: { productDetail: MOCK_PRODUCT_DETAIL }
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
    ) as jasmine.SpyObj<RoutingQueryParamService>;

    languageService = TestBed.inject(
      LanguageService
    ) as jasmine.SpyObj<LanguageService>;

    sanitizerSpy = TestBed.inject(DomSanitizer) as jasmine.SpyObj<DomSanitizer>;

    titleService = TestBed.inject(Title);
    mockProductFeedbackService.getInitFeedbacksObservable.and.returnValue(
      of([MOCK_FEEDBACK_API_RESPONSE] as any as FeedbackApiResponse)
    );

    mockProductFeedbackService.findProductFeedbackOfUser.and.returnValue(
      of({} as any as Feedback[])
    );
    mockAppModalService.openAddFeedbackDialog.and.returnValue(
      Promise.resolve()
    );
    mockAuthService.getToken.and.returnValue('token');
    mockProductStarRatingService.getRatingObservable.and.returnValue(
      of([] as any as StarRatingCounting[])
    );
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductDetailComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
    if ((window.scrollTo as any).calls) {
      (window.scrollTo as jasmine.Spy).and.callThrough();
    }
  });

  afterEach(() => {
    if ((window.scrollTo as any).calls) {
      (window.scrollTo as jasmine.Spy).and.callThrough();
    }
  });

  it('should create', () => {
    expect(component.productDetail().names['en']).toEqual(
      MOCK_PRODUCT_DETAIL.names['en']
    );
  });

  it('should open add feedback dialog if token is present', () => {
    languageService.selectedLanguage.and.returnValue(Language.DE);
    mockAuthService.getToken.and.returnValue('token');

    component.onClickRateBtn();
    fixture.detectChanges();

    expect(mockAppModalService.openAddFeedbackDialog).toHaveBeenCalled();
  });

  it('should redirect to Gitub if token is null', () => {
    languageService.selectedLanguage.and.returnValue(Language.DE);
    mockAuthService.getToken.and.returnValue(null);

    component.onClickRateBtn();
    fixture.detectChanges();

    expect(mockAuthService.redirectToGitHub).toHaveBeenCalled();
  });

  it('should have title like the name DE', () => {
    languageService.selectedLanguage.and.returnValue(Language.DE);
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

  it('should call keepCurrentTabScroll when setActiveTab is called', () => {
    spyOn(component, 'keepCurrentTabScroll');
    const tab = 'specifications';
    component.setActiveTab(tab);
    expect(component.keepCurrentTabScroll).toHaveBeenCalledWith(tab);
  });

  it('should call setActiveTab and updateDropdownSelection on setActiveTab', () => {
    const event = { value: 'description' };
    spyOn(component, 'setActiveTab');
    spyOn(component, 'updateDropdownSelection');

    component.setActiveTab(event.value);

    expect(component.setActiveTab).toHaveBeenCalledWith('description');
  });

  it('should not display information when product detail is empty', () => {
    const mockContentWithEmptySetup: ProductModuleContent =
      {} as ProductModuleContent;
    component.productModuleContent.set(mockContentWithEmptySetup);
    expect(component.isEmptyProductContent()).toBeTrue();
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

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return true for description when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { de: 'Test description' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return true for description when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return true for description when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return false for description when it is null', () => {
    const mockContentWithNullDescription: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullDescription);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for any tab when detail content is undefined or null', () => {
    component.productModuleContent.set(null as any as ProductModuleContent);
    expect(component.getContent('description')).toBeFalse();
    component.productModuleContent.set(
      undefined as any as ProductModuleContent
    );
    expect(component.getContent('description')).toBeFalse();
    component.productModuleContent.set({} as any as ProductModuleContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: '', de: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { de: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in both DE and EN language it is an undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return true for setup when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return true for setup when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { de: 'Test setup' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return true for setup when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return true for setup when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return false for setup when it is null', () => {
    const mockContentWithNullSetup: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullSetup);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: '', de: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { de: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in both DE and EN language it is an undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return true for demo when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return true for demo when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { de: 'Test demo' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return true for demo when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return true for demo when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return false for demo when it is null', () => {
    const mockContentWithNullDemo: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullDemo);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: '', de: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { de: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in both DE and EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for changelog when productReleaseSafeHtmls is empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };

    component.productReleaseSafeHtmls = signal([]);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('changelog')).toBeFalse();
  });

  it('should display dropdown horizontally on small viewport', () => {
    viewport.set(540);
    const tabGroup = fixture.debugElement.query(By.css('.row-tab'));
    tabGroup.triggerEventHandler('click', null);

    fixture.detectChanges();
    const dropdown = fixture.debugElement.query(By.css('.dropdown-tab'));

    expect(getComputedStyle(dropdown.nativeElement).flexDirection).toBe('row');
  });

  it('should display dropdown instead of tabs when viewport width is 540px', () => {
    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const tabs = tabGroup.query(By.css('.row-tab d-none d-xl-block col-12'));
    const dropdown = tabGroup.query(By.css('.dropdown-tab'));

    expect(tabs).toBeFalsy();
    expect(dropdown).toBeTruthy();
  });

  it('should display tabs instead of dropdown when viewport width is above 540px', () => {
    viewport.set(1920);
    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const dropdown = tabGroup.query(
      By.css(
        '.dropdown-tab d-block d-xl-none d-flex flex-row justify-content-center align-items-center w-100'
      )
    );

    expect(dropdown).toBeFalsy();
  });

  it('should display info tab on click of info icon for smaller screens', () => {
    viewport.set(540);

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

  it('should call checkMediaSize on ngOnInit', fakeAsync(() => {
    spyOn(component, 'checkMediaSize');
    component.ngOnInit();
    tick();
    expect(component.checkMediaSize).toHaveBeenCalled();
  }));

  it('should set isMobileMode based on window size', () => {
    spyOn(window, 'matchMedia').and.returnValue({
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
    expect(component.isMobileMode()).toBeTrue();

    (window.matchMedia as jasmine.Spy).and.returnValue({
      matches: false,
      media: '',
      addEventListener: () => {},
      removeEventListener: () => {}
    });

    component.checkMediaSize();
    expect(component.isMobileMode()).toBeFalse();
  });

  it('should call checkMediaSize on window resize', () => {
    spyOn(component, 'checkMediaSize');
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
    routingQueryParamService.isDesignerEnv.and.returnValue(true);
    component.handleProductContentVersion();
    expect(component.selectedVersion).toEqual('Version 10.0.11');
  });

  it('should return DESIGNER_ENV as acction type in Designer Env', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(true);

    component.updateProductDetailActionType({ sourceUrl: 'some-url' } as any);
    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.DESIGNER_ENV
    );
  });

  it('should return CUSTOM_SOLUTION as acction type when productDetail.sourceUrl is undefined', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(false);

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

  it('should return STANDARD as acction type when when productDetail.sourceUrl is defined', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(false);

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
    expect(rateConnector.childNodes[0].nativeNode.textContent).toContain(
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
    sanitizerSpy.bypassSecurityTrustHtml.and.returnValue(mockedRenderedHtml);

    const result = component.renderGithubAlert(value);

    expect(result).toBe(mockedRenderedHtml);
  });

  it('should process README content correctly with different values per tab', () => {
    languageService.selectedLanguage.and.returnValue(Language.EN);

    spyOn(component, 'getProductModuleContentValue').and.callFake(tab => {
      const key = tab.value as keyof ProductModuleContent;
      return MOCK_PRODUCT_DETAIL.productModuleContent[key] as { en: string };
    });

    spyOn(MultilingualismPipe.prototype, 'transform').and.callFake(
      content => `${content}`
    );
    spyOn(component, 'renderGithubAlert').and.callFake(
      (content: string) => `${content}` as SafeHtml
    );

    component.getReadmeContent();

    expect(component.loadedReadmeContent['description']).toBe(
      `${MOCK_PRODUCT_DETAIL.productModuleContent.description}`
    );
    expect(component.loadedReadmeContent['demo']).toBe(
      `${MOCK_PRODUCT_DETAIL.productModuleContent.demo}`
    );

    expect(component.getProductModuleContentValue).toHaveBeenCalled();
    expect(MultilingualismPipe.prototype.transform).toHaveBeenCalled();
    expect(component.renderGithubAlert).toHaveBeenCalled();
  });

  it('should not process content if getProductModuleContentValue returns null', () => {
    spyOn(component, 'getProductModuleContentValue').and.returnValue(null);
    spyOn(component, 'renderGithubAlert');

    component.getReadmeContent();

    expect(component.getProductModuleContentValue).toHaveBeenCalledTimes(
      PRODUCT_DETAIL_TABS.length
    );
    expect(component.renderGithubAlert).not.toHaveBeenCalled();
  });

  it('should close the dropdown when clicking outside', fakeAsync(() => {
    component.isDropdownOpen.set(true);
    fixture.detectChanges();
    tick();

    const outsideElement = document.createElement('div');
    document.body.appendChild(outsideElement);
    outsideElement.click();

    fixture.detectChanges();
    tick();

    expect(component.isDropdownOpen()).toBeFalse();

    document.body.removeChild(outsideElement);
  }));

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
    sanitizerSpy.bypassSecurityTrustHtml.and.returnValue(expectedSafeHtml);

    const result = component.renderChangelogContent(mockReleases);

    expect(result[0].body).toBe(expectedSafeHtml);
  });

  it('should navigate with correct query parameters when last recorded values differ from defaults', () => {
    mockHistoryService.lastSearchType.and.returnValue(TypeOption.CONNECTORS);
    mockHistoryService.lastSortOption.and.returnValue(SortOption.POPULARITY);
    mockHistoryService.lastSearchText.and.returnValue('test-search');

    component.navigateToHomePageWithLastSearch();

    expect(mockRouter.navigate).toHaveBeenCalledWith([API_URI.APP], {
      relativeTo: jasmine.anything(),
      queryParamsHandling: 'merge',
      queryParams: {
        type: TypeOption.CONNECTORS,
        sort: SortOption.POPULARITY,
        search: 'test-search'
      }
    });
  });

  it('should navigate with empty query params when defaults are present', () => {
    mockHistoryService.lastSearchType.and.returnValue(TypeOption.All_TYPES);
    mockHistoryService.lastSortOption.and.returnValue(SortOption.STANDARD);
    mockHistoryService.lastSearchText.and.returnValue('');

    component.navigateToHomePageWithLastSearch();

    expect(mockRouter.navigate).toHaveBeenCalledWith([API_URI.APP], {
      relativeTo: jasmine.anything(),
      queryParamsHandling: 'merge',
      queryParams: {}
    });
  });

  it('should navigate to home page when click back to button regardless history', () => {
    mockHistoryService.lastSearchType.and.returnValue(TypeOption.All_TYPES);
    mockHistoryService.lastSortOption.and.returnValue(SortOption.STANDARD);
    mockHistoryService.lastSearchText.and.returnValue('');
    component.setActiveTab('description');
    component.setActiveTab('demo');
    component.setActiveTab('setup');
    component.onClickingBackToHomepageButton();
    expect(mockRouter.navigate).toHaveBeenCalledWith([API_URI.APP]);
  });

  it('should get tab value from fragment', () => {
    const tabValue = component.getTabValueFromFragment('tab-description');
    expect(tabValue).toBe('description');
  });

  it('should return default tab value if fragment is invalid', () => {
    const tabValue = component.getTabValueFromFragment('tab-invalid');
    expect(tabValue).toBe(PRODUCT_DETAIL_TABS[0].value);
  });

  it('should call setActiveTab with correct tab value from fragment', () => {
    spyOn(component, 'setActiveTab');
    component.navigateToProductDetailsWithTabFragment();
    expect(component.setActiveTab).toHaveBeenCalledWith('description');
  });

  it('should restore scroll position for a tab', () => {
    component.isBrowser = true;
    const tabId = 'demo';
    component['scrollPositions'][tabId] = 1234;
    spyOn(window, 'scrollTo');
    component.keepCurrentTabScroll(tabId);
    expect(window.scrollTo).toHaveBeenCalledWith(0, 1234);
  });

  describe('loadChangelogs', () => {
    let productService: jasmine.SpyObj<ProductService>;

    beforeEach(() => {
      productService = jasmine.createSpyObj('ProductService', [
        'getProductChangelogs'
      ]);
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
      productService.getProductChangelogs.and.returnValue(
        of(mockResponse as any)
      );
      spyOn(component, 'renderChangelogContent').and.returnValue([
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
      productService.getProductChangelogs.and.returnValue(of(null as any));

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
      productService.getProductChangelogs.and.returnValue(
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
      productService.getProductChangelogs.and.returnValue(
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
      productService.getProductChangelogs.and.returnValue(
        new Observable((subscriber: any) => {
          subscriber.error(errorResponse);
        })
      );

      spyOn(console, 'error'); // Prevent error logging in test output

      // Act & Assert
      expect(() => component.loadChangelogs()).not.toThrow();
      expect(productService.getProductChangelogs).toHaveBeenCalledWith(
        component.criteria
      );
    });
  });

  describe('setupIntersectionObserver', () => {
    let mockObserver: jasmine.SpyObj<IntersectionObserver>;
    let mockObserverElement: jasmine.SpyObj<ElementRef>;
    let originalIntersectionObserver: any;

    beforeEach(() => {
      mockObserver = jasmine.createSpyObj('IntersectionObserver', ['observe']);
      mockObserverElement = jasmine.createSpyObj('ElementRef', [], {
        nativeElement: document.createElement('div')
      });

      // Store original IntersectionObserver and mock it
      originalIntersectionObserver = (window as any).IntersectionObserver;
      (window as any).IntersectionObserver = function (
        callback: IntersectionObserverCallback,
        options?: IntersectionObserverInit
      ) {
        // Store the callback for testing
        (window as any).IntersectionObserver._lastCallback = callback;
        return mockObserver;
      };
      spyOn(window as any, 'IntersectionObserver').and.callThrough();
    });

    afterEach(() => {
      // Restore original IntersectionObserver
      (window as any).IntersectionObserver = originalIntersectionObserver;
    });

    it('should return early if observerElement is not available', () => {
      // Arrange
      component.observerElement = undefined as any;
      component.isBrowser = true;

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((window as any).IntersectionObserver).not.toHaveBeenCalled();
    });

    it('should return early if changelogIntersectionObserver already exists', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      (component as any).changelogIntersectionObserver = mockObserver;
      component.isBrowser = true;

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((window as any).IntersectionObserver).not.toHaveBeenCalled();
    });

    it('should return early if not in browser environment', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = false;

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((window as any).IntersectionObserver).not.toHaveBeenCalled();
    });

    it('should return early if IntersectionObserver is not supported', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (window as any).IntersectionObserver = undefined;

      // Act & Assert - In this case we can't check if it was called since it's undefined
      // The test passes if no error is thrown
      expect(() => component.setupIntersectionObserver()).not.toThrow();
    });

    it('should create IntersectionObserver with correct options and observe element', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;
      ((window as any).IntersectionObserver as jasmine.Spy).and.returnValue(
        mockObserver
      );

      // Act
      component.setupIntersectionObserver();

      // Assert
      expect((window as any).IntersectionObserver).toHaveBeenCalledWith(
        jasmine.any(Function),
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

      spyOn(component, 'hasMoreChangelogs').and.returnValue(true);
      spyOn(component, 'loadChangelogs');

      component.changeLogLinks = { next: { href: 'next-page-url' } } as any;

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = (window as any).IntersectionObserver
        ._lastCallback;

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

      observerCallback(mockEntries, mockObserver);

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

      spyOn(component, 'hasMoreChangelogs').and.returnValue(false);
      spyOn(component, 'loadChangelogs');

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = (window as any).IntersectionObserver
        ._lastCallback;

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

      observerCallback(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).toHaveBeenCalled();
      expect(component.loadChangelogs).not.toHaveBeenCalled();
    });

    it('should not call loadChangelogs when entry is not intersecting', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      spyOn(component, 'hasMoreChangelogs').and.returnValue(true);
      spyOn(component, 'loadChangelogs');

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = (window as any).IntersectionObserver
        ._lastCallback;

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

      observerCallback(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).not.toHaveBeenCalled();
      expect(component.loadChangelogs).not.toHaveBeenCalled();
    });

    it('should not call loadChangelogs when isLoading', () => {
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      component.loadingService.showLoading(LoadingComponentId.PRODUCT_CHANGELOG);
      (component as any).changelogIntersectionObserver = undefined;
      spyOn(component, 'hasMoreChangelogs').and.returnValue(true);
      spyOn(component, 'loadChangelogs');

      component.setupIntersectionObserver();
      const observerCallback = (window as any).IntersectionObserver
        ._lastCallback;
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
      observerCallback(mockEntries, mockObserver);
      expect(component.loadChangelogs).not.toHaveBeenCalled();
    });  

    it('should handle multiple entries in intersection callback', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      spyOn(component, 'hasMoreChangelogs').and.returnValue(true);
      spyOn(component, 'loadChangelogs');

      component.changeLogLinks = { next: { href: 'next-page-url' } } as any;

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = (window as any).IntersectionObserver
        ._lastCallback;

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

      observerCallback(mockEntries, mockObserver);

      // Assert
      expect(component.hasMoreChangelogs).toHaveBeenCalledTimes(2);
      expect(component.loadChangelogs).toHaveBeenCalledTimes(2);
    });

    it('should set nextPageHref correctly when intersection occurs', () => {
      // Arrange
      component.observerElement = mockObserverElement;
      component.isBrowser = true;
      (component as any).changelogIntersectionObserver = undefined;

      spyOn(component, 'hasMoreChangelogs').and.returnValue(true);
      spyOn(component, 'loadChangelogs');

      const expectedHref = 'http://example.com/next-page';
      component.changeLogLinks = { next: { href: expectedHref } } as any;
      component.criteria.nextPageHref = 'initial-href';

      // Act
      component.setupIntersectionObserver();

      // Get the stored callback
      const observerCallback = (window as any).IntersectionObserver
        ._lastCallback;

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

      observerCallback(mockEntries, mockObserver);

      // Assert
      expect(component.criteria.nextPageHref).toBe(expectedHref);
      expect(component.loadChangelogs).toHaveBeenCalled();
    });
  });
});
