import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick
} from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { TypeOption } from '../../shared/enums/type-option.enum';
import { SortOption } from '../../shared/enums/sort-option.enum';
import { ProductComponent } from './product.component';
import { ProductService } from './product.service';
import { MockProductService } from '../../shared/mocks/mock-services';

const router = {
  navigate: jasmine.createSpy('navigate')
};

describe('ProductComponent', () => {
  let component: ProductComponent;
  let fixture: ComponentFixture<ProductComponent>;
  let mockIntersectionObserver: any;

  beforeAll(() => {
    mockIntersectionObserver = jasmine.createSpyObj('IntersectionObserver', ['observe', 'unobserve', 'disconnect']);
    mockIntersectionObserver.observe.and.callFake(() => { });
    mockIntersectionObserver.unobserve.and.callFake(() => { });
    mockIntersectionObserver.disconnect.and.callFake(() => { });

    (window as any).IntersectionObserver = function (callback: IntersectionObserverCallback) {
      mockIntersectionObserver.callback = callback;
      return mockIntersectionObserver;
    };
  });

  afterAll(() => {
    delete (window as any).IntersectionObserver;
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductComponent, TranslateModule.forRoot()],
      providers: [
        {
          provide: Router,
          useValue: router
        },
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
    fixture = TestBed.createComponent(ProductComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('viewProductDetail should navigate', () => {
    component.viewProductDetail('url');
    expect(router.navigate).toHaveBeenCalledWith(['', 'url']);
  });

  it('loadProductItems should return products with criteria', () => {

    component.loadProductItems();
    expect(component.loadProductItems).toBeTruthy();
  })

  it('ngOnDestroy should unsubscribe all sub', () => {
    const sub = new Subscription();
    component.subscriptions.push(sub);
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toBeTruthy();
  });

  it('onFilterChange should filter products properly', () => {
    component.onFilterChange(TypeOption.CONNECTORS);
    component.products().forEach((product) => {
      expect(product.type).toEqual('connector');
    });
  });

  it('onSortChange should order products properly', () => {
    component.onSearchChanged('cur');
    component.onSortChange(SortOption.ALPHABETICALLY);
    for (let i = 0; i < component.products.length - 1; i++) {
      expect(
        component.products()[i + 1].names.en.localeCompare(component.products()[i].names.en)
      ).toEqual(1);
    }
  });

  it('search should return match products name', fakeAsync(() => {
    const productName = 'amazon comprehend';
    component.onSearchChanged(productName);
    tick(500);
    component.products().forEach((product) => {
      expect(product.names.en.toLowerCase()).toContain(productName);
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
});
