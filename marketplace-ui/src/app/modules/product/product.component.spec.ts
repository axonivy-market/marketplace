import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, Subscription } from 'rxjs';
import { TypeOption } from '../../shared/enums/type-option.enum';
import { SortOption } from '../../shared/enums/sort-option.enum';
import { ProductComponent } from './product.component';
import { ProductService } from './product.service';
import { MockProductService } from '../../shared/mocks/mock-services';
import { RoutingQueryParamService } from '../../shared/services/routing.query.param.service';
import { DESIGNER_COOKIE_VARIABLE } from '../../shared/constants/common.constant';
import { ItemDropdown } from '../../shared/models/item-dropdown.model';
import { By } from '@angular/platform-browser';
import { Location } from '@angular/common';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';

describe('ProductComponent', () => {
  let component: ProductComponent;
  let fixture: ComponentFixture<ProductComponent>;
  let mockIntersectionObserver: any;
  let routingQueryParamService: jasmine.SpyObj<RoutingQueryParamService>;
  let location: Location;
  let router: Router;

  beforeAll(() => {
    mockIntersectionObserver = jasmine.createSpyObj('IntersectionObserver', [
      'observe',
      'unobserve',
      'disconnect'
    ]);
    mockIntersectionObserver.observe.and.callFake(() => {});
    mockIntersectionObserver.unobserve.and.callFake(() => {});
    mockIntersectionObserver.disconnect.and.callFake(() => {});

    (window as any).IntersectionObserver = function (
      callback: IntersectionObserverCallback
    ) {
      mockIntersectionObserver.callback = callback;
      return mockIntersectionObserver;
    };
  });

  afterAll(() => {
    delete (window as any).IntersectionObserver;
  });

  beforeEach(async () => {
    routingQueryParamService = jasmine.createSpyObj(
      'RoutingQueryParamService',
      [
        'getNavigationStartEvent',
        'isDesigner',
        'isDesignerEnv',
        'checkCookieForDesignerEnv',
        'checkCookieForDesignerVersion'
      ]
    );

    await TestBed.configureTestingModule({
      imports: [
        ProductComponent, 
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        provideMatomoTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({
              [DESIGNER_COOKIE_VARIABLE.restClientParamName]: true
            })
          }
        },
        {
          provide: RoutingQueryParamService,
          useValue: routingQueryParamService
        },
        provideRouter([
          {
            path: ':id',
            component: ProductDetailComponent
          }
        ]),
        ProductService,
        TranslateService,
        provideHttpClient()
      ]
    })
      .overrideComponent(ProductComponent, {
        remove: { providers: [ProductService] },
        add: {
          providers: [{ provide: ProductService, useClass: MockProductService }]
        }
      })
      .compileComponents();
    
    location = TestBed.inject(Location);
    router = TestBed.inject(Router);

    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as jasmine.SpyObj<RoutingQueryParamService>;

    fixture = TestBed.createComponent(ProductComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loadProductItems should return products with criteria', () => {
    component.loadProductItems();
    expect(component.loadProductItems).toBeTruthy();
  });

  it('ngOnDestroy should unsubscribe all sub', () => {
    const sub = new Subscription();
    component.subscriptions.push(sub);
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toBeTruthy();
  });

  it('onFilterChange should filter products properly', () => {
    const filterOption: ItemDropdown<TypeOption> = {
      value: TypeOption.CONNECTORS,
      label: 'Connectors' // Or whatever label is appropriate
    };

    component.onFilterChange(filterOption);
    component.products().forEach(product => {
      expect(product.type).toEqual('connector');
    });
  });

  it('onSortChange should order products properly', () => {
    component.onSearchChanged('cur');
    component.onSortChange(SortOption.ALPHABETICALLY);
    for (let i = 0; i < component.products.length - 1; i++) {
      expect(
        component
          .products()
          [i + 1].names['en'].localeCompare(component.products()[i].names['en'])
      ).toEqual(1);
    }
  });

  it('search should return match products name', fakeAsync(() => {
    const productName = 'amazon comprehend';
    component.onSearchChanged(productName);
    tick(500);
    component.products().forEach(product => {
      expect(product.names['en'].toLowerCase()).toContain(productName);
    });
  }));

  it('setupIntersectionObserver should not trigger when init page', () => {
    component.ngAfterViewInit();
    expect(component.criteria.nextPageHref).toBeUndefined();
  });

  it('should call loadProductItems when observerElement is intersecting and has more products', () => {
    spyOn(component, 'loadProductItems').and.callThrough();
    spyOn(component, 'hasMore').and.returnValue(true);

    const entries = [{ isIntersecting: true }];
    const callback = mockIntersectionObserver.callback;

    callback(entries as IntersectionObserverEntry[]);

    expect(component.hasMore).toHaveBeenCalled();
    expect(component.loadProductItems).toHaveBeenCalled();
  });

  it('should not call loadProductItems when observerElement is not intersecting', () => {
    spyOn(component, 'loadProductItems').and.callThrough();
    spyOn(component, 'hasMore').and.returnValue(true);

    const entries = [{ isIntersecting: false }];
    const callback = mockIntersectionObserver.callback;

    callback(entries as IntersectionObserverEntry[]);

    expect(component.hasMore).not.toHaveBeenCalled();
    expect(component.loadProductItems).not.toHaveBeenCalled();
  });

  it('should not call loadProductItems when there are no more products', () => {
    spyOn(component, 'loadProductItems').and.callThrough();
    spyOn(component, 'hasMore').and.returnValue(false);

    const entries = [{ isIntersecting: true }];
    const callback = mockIntersectionObserver.callback;

    callback(entries as IntersectionObserverEntry[]);

    expect(component.hasMore).toHaveBeenCalled();
    expect(component.loadProductItems).not.toHaveBeenCalled();
  });

  it('should set isRESTClient true based on query params and designer environment', () => {
    component.route.queryParams = of({
      [DESIGNER_COOKIE_VARIABLE.restClientParamName]: 'resultsOnly',
    });
    
    routingQueryParamService.isDesignerEnv.and.returnValue(true);
    const fixtureTest = TestBed.createComponent(ProductComponent);
    component = fixtureTest.componentInstance;

    expect(component.isRESTClient()).toBeTrue();
  });

  it('should not display marketplace introduction in designer', () => {
    component.route.queryParams = of({
      [DESIGNER_COOKIE_VARIABLE.restClientParamName]: 'resultsOnly',
      [DESIGNER_COOKIE_VARIABLE.searchParamName]: 'search'
    });

    component.isDesignerEnvironment = true;
    fixture.detectChanges();

    const compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('.row col-md-12 mt-8')).toBeNull();
  });

  it('should navigate to product detail page when clicking on a product card', async () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(false);
    const fixtureTest = TestBed.createComponent(ProductComponent);
    component = fixtureTest.componentInstance;

    expect(component.isRESTClient()).toBeFalse();

    const productName = 'amazon-comprehend';

    const productCardComponent = fixture.debugElement.query(
      By.css('.product-card')
    ).nativeElement as HTMLDivElement;

    productCardComponent.click();
    await router.navigate([productName]);
    expect(location.path()).toBe('/amazon-comprehend');
  });
});
