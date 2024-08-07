import { TestBed, ComponentFixture } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import { ActivatedRoute, RouterOutlet, NavigationStart } from '@angular/router';
import { of, Subject } from 'rxjs';
import { TranslateService, TranslateModule } from '@ngx-translate/core';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let routingQueryParamService: jasmine.SpyObj<RoutingQueryParamService>;
  let activatedRoute: ActivatedRoute;
  let navigationStartSubject: Subject<NavigationStart>;

  beforeEach(async () => {
    navigationStartSubject = new Subject<NavigationStart>();
    const loadingServiceSpy = jasmine.createSpyObj('LoadingService', [
      'isLoading'
    ]);
    const routingQueryParamServiceSpy = jasmine.createSpyObj(
      'RoutingQueryParamService',
      [
        'getNavigationStartEvent',
        'isDesignerEnv',
        'checkCookieForDesignerEnv',
        'checkCookieForDesignerVersion'
      ]
    );

    await TestBed.configureTestingModule({
      imports: [
        AppComponent,
        RouterOutlet,
        HeaderComponent,
        FooterComponent,
        TranslateModule.forRoot()
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
        TranslateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as jasmine.SpyObj<RoutingQueryParamService>;
    activatedRoute = TestBed.inject(ActivatedRoute);

    routingQueryParamService.getNavigationStartEvent.and.returnValue(
      navigationStartSubject.asObservable()
    );
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should subscribe to query params and check cookies if not in designer environment', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(false);
    const params = { someParam: 'someValue' };

    // Mock the queryParams observable to emit params
    (activatedRoute.queryParams as any) = of(params);

    // Trigger the lifecycle hooks
    fixture.detectChanges();

    // Trigger the navigation start event
    navigationStartSubject.next(new NavigationStart(1, 'testUrl'));

    expect(
      routingQueryParamService.checkCookieForDesignerEnv
    ).toHaveBeenCalledWith(params);
    expect(
      routingQueryParamService.checkCookieForDesignerVersion
    ).toHaveBeenCalledWith(params);
  });

  it('should not subscribe to query params if in designer environment', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(true);

    // Trigger the navigation start event
    navigationStartSubject.next(new NavigationStart(1, 'testUrl'));

    expect(
      routingQueryParamService.checkCookieForDesignerEnv
    ).not.toHaveBeenCalled();
    expect(
      routingQueryParamService.checkCookieForDesignerVersion
    ).not.toHaveBeenCalled();
  });
});
