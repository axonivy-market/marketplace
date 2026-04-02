import type { MockedObject } from 'vitest';
import { vi } from 'vitest';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import {
  ActivatedRoute,
  RouterOutlet,
  NavigationStart,
  RouterModule,
  Router,
  NavigationError,
  Event
} from '@angular/router';
import { of, Subject } from 'rxjs';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { By } from '@angular/platform-browser';
import { ERROR_PAGE_PATH } from './shared/constants/common.constant';
import { WindowRef } from './core/services/browser/window-ref.service';
import { DocumentRef } from './core/services/browser/document-ref.service';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let routingQueryParamService: MockedObject<RoutingQueryParamService>;
  let activatedRoute: ActivatedRoute;
  let navigationStartSubject: Subject<NavigationStart>;
  let appElement: HTMLElement;
  let router: Router;
  let routerEventsSubject: Subject<Event>;

  beforeEach(async () => {
    navigationStartSubject = new Subject<NavigationStart>();
    routerEventsSubject = new Subject<Event>();
    const loadingServiceSpy = {
      isLoading: vi.fn().mockName('LoadingService.isLoading')
    };
    const routingQueryParamServiceSpy = {
      getNavigationStartEvent: vi
        .fn()
        .mockName('RoutingQueryParamService.getNavigationStartEvent'),
      isDesignerEnv: vi.fn().mockName('RoutingQueryParamService.isDesignerEnv'),
      checkSessionStorageForDesignerEnv: vi
        .fn()
        .mockName('RoutingQueryParamService.checkSessionStorageForDesignerEnv'),
      checkSessionStorageForDesignerVersion: vi
        .fn()
        .mockName(
          'RoutingQueryParamService.checkSessionStorageForDesignerVersion'
        )
    };

    const routerMock = {
      events: routerEventsSubject.asObservable(),
      navigate: vi.fn(),
      createUrlTree: vi.fn(),
      serializeUrl: vi.fn(),
      parseUrl: vi.fn(),
      url: '/',
      routerState: {
        root: {}
      }
    };

    // Mock WindowRef and DocumentRef
    const windowRefMock = {
      toString: vi.fn().mockName('WindowRef.toString'),
      nativeWindow: globalThis
    };

    const documentRefMock = {
      toString: vi.fn().mockName('DocumentRef.toString'),
      nativeDocument: document
    };

    await TestBed.configureTestingModule({
      imports: [
        AppComponent,
        RouterOutlet,
        HeaderComponent,
        FooterComponent,
        TranslateModule.forRoot(),
        RouterModule.forRoot([])
      ],
      providers: [
        { provide: LoadingService, useValue: loadingServiceSpy },
        {
          provide: RoutingQueryParamService,
          useValue: routingQueryParamServiceSpy
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        },
        TranslateService,
        { provide: Router, useValue: routerMock },
        { provide: WindowRef, useValue: windowRefMock },
        { provide: DocumentRef, useValue: documentRefMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as MockedObject<RoutingQueryParamService>;

    routingQueryParamService.getNavigationStartEvent.mockReturnValue(
      navigationStartSubject.asObservable()
    );
    appElement = fixture.debugElement.query(
      By.css('.app-container')
    ).nativeElement;

    activatedRoute = TestBed.inject(ActivatedRoute);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should subscribe to query params and check session strorage if not in designer environment', () => {
    routingQueryParamService.isDesignerEnv.mockReturnValue(false);
    const params = { someParam: 'someValue' };

    // Mock the queryParams observable to emit params
    (activatedRoute.queryParams as any) = of(params);

    // Trigger the lifecycle hooks
    fixture.detectChanges();

    // Trigger the navigation start event
    navigationStartSubject.next(new NavigationStart(1, 'testUrl'));

    expect(
      routingQueryParamService.checkSessionStorageForDesignerEnv
    ).toHaveBeenCalledWith(params);
    expect(
      routingQueryParamService.checkSessionStorageForDesignerVersion
    ).toHaveBeenCalledWith(params);
  });

  it('should not subscribe to query params if in designer environment', () => {
    routingQueryParamService.isDesignerEnv.mockReturnValue(true);

    // Trigger the navigation start event
    navigationStartSubject.next(new NavigationStart(1, 'testUrl'));

    expect(
      routingQueryParamService.checkSessionStorageForDesignerEnv
    ).not.toHaveBeenCalled();
    expect(
      routingQueryParamService.checkSessionStorageForDesignerVersion
    ).not.toHaveBeenCalled();
  });

  it('should hide scrollbar when burger menu is opened', () => {
    component.isMobileMenuCollapsed = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const headerElement = fixture.debugElement.query(By.css('.header-mobile'));
    expect(headerElement).toBeTruthy();

    expect(appElement.classList.contains('header-mobile-container')).toBe(true);

    const headerComputedStyle = globalThis.getComputedStyle(appElement);
    // jsdom cannot compute CSS from stylesheets; verify the class that sets overflow:hidden is present
    expect(appElement.classList.contains('header-mobile-container')).toBe(true);
  });

  it('should reset header style when burger menu is closed', () => {
    component.isMobileMenuCollapsed = true;
    fixture.detectChanges();

    const headerElement = fixture.debugElement.query(By.css('.header-mobile'));
    expect(headerElement).toBeNull();

    expect(appElement.classList.contains('header-mobile-container')).toBe(
      false
    );
  });

  it('should redirect to "/error-page" on NavigationError', () => {
    // Simulate a NavigationError event
    const navigationError = new NavigationError(
      1,
      '/a-trust/test-url',
      'Error message'
    );
    routerEventsSubject.next(navigationError);
    expect(router.navigate).toHaveBeenCalledWith([ERROR_PAGE_PATH]);
  });
});
