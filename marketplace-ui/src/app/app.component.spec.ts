import { TestBed, ComponentFixture } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import { ActivatedRoute, RouterOutlet, NavigationStart, RouterModule, Router, NavigationError, Event } from '@angular/router';
import { of, Subject } from 'rxjs';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { By } from '@angular/platform-browser';
import { ERROR_PAGE_PATH } from './shared/constants/common.constant';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let routingQueryParamService: jasmine.SpyObj<RoutingQueryParamService>;
  let activatedRoute: ActivatedRoute;
  let navigationStartSubject: Subject<NavigationStart>;
  let appElement: HTMLElement;
  let router: Router;
  let routerEventsSubject: Subject<Event>;

  beforeEach(async () => {
    navigationStartSubject = new Subject<NavigationStart>();
    routerEventsSubject = new Subject<Event>();
    const loadingServiceSpy = jasmine.createSpyObj('LoadingService', [
      'isLoading'
    ]);
    const routingQueryParamServiceSpy = jasmine.createSpyObj(
      'RoutingQueryParamService',
      [
        'getNavigationStartEvent',
        'isDesignerEnv',
        'checkSessionStorageForDesignerEnv',
        'checkSessionStorageForDesignerVersion'
      ]
    );

    const routerMock = {
      events: routerEventsSubject.asObservable(),
      navigate: jasmine.createSpy('navigate'),
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
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as jasmine.SpyObj<RoutingQueryParamService>;

    routingQueryParamService.getNavigationStartEvent.and.returnValue(
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
    routingQueryParamService.isDesignerEnv.and.returnValue(false);
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
    routingQueryParamService.isDesignerEnv.and.returnValue(true);

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
    fixture.detectChanges();

    const headerElement = fixture.debugElement.query(By.css('.header-mobile'));
    expect(headerElement).toBeTruthy();

    expect(appElement.classList.contains('header-mobile-container')).toBeTrue();

    const headerComputedStyle = window.getComputedStyle(appElement);
    expect(headerComputedStyle.overflow).toBe('hidden');
  });

  it('should reset header style when burger menu is closed', () => {
    component.isMobileMenuCollapsed = true;
    fixture.detectChanges();

    const headerElement = fixture.debugElement.query(By.css('.header-mobile'));
    expect(headerElement).toBeNull();

    expect(
      appElement.classList.contains('header-mobile-container')
    ).toBeFalse();
  });

  it('should redirect to "/error-page" on NavigationError', () => {
    // Simulate a NavigationError event
    const navigationError = new NavigationError(1, '/a-trust/test-url', 'Error message');
    routerEventsSubject.next(navigationError);
    expect(router.navigate).toHaveBeenCalledWith([ERROR_PAGE_PATH]);
  });
});
