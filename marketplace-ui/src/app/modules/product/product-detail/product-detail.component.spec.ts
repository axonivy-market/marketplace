import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { MarkdownModule } from 'ngx-markdown';
import { of } from 'rxjs';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import {
  MOCK_PRODUCT_DETAILS,
  MOCK_PRODUCT_MODULE_CONTENT,
  MOCK_PRODUCTS
} from '../../../shared/mocks/mock-data';
import { MockProductService } from '../../../shared/mocks/mock-services';
import { ProductService } from '../product.service';
import { ProductDetailComponent } from './product-detail.component';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';

const products = MOCK_PRODUCTS._embedded.products;
declare const viewport: Viewport;

describe('ProductDetailComponent', () => {
  let component: ProductDetailComponent;
  let fixture: ComponentFixture<ProductDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProductDetailComponent,
        TranslateModule.forRoot(),
        MarkdownModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: products[0].id },
              queryParams: { type: TypeOption.CONNECTORS }
            },
            fragment: of('description')
          }
        }
      ]
    })
      .overrideComponent(ProductDetailComponent, {
        remove: { providers: [ProductService] },
        add: {
          providers: [{ provide: ProductService, useClass: MockProductService }]
        }
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component.productDetail().names['en']).toEqual(
      MOCK_PRODUCT_DETAILS.names['en']
    );
  });

  it('should toggle isDropdownOpen on onShowDropdown', () => {
    component.isDropdownOpen.set(false);
    component.onShowInfoContent();
    expect(component.isDropdownOpen()).toBe(true);

    component.onShowInfoContent();
    expect(component.isTabDropdownShown()).toBe(false);
  });

  it('should reset state before fetching new product details', () => {
    component.productDetail.set(MOCK_PRODUCT_DETAILS);
    component.productModuleContent.set(
      MOCK_PRODUCT_DETAILS.productModuleContent
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

  it('should call updateDropdownSelection when setActiveTab is called', () => {
    spyOn(component, 'updateDropdownSelection');
    const tab = 'specifications';
    component.setActiveTab(tab);
    expect(component.updateDropdownSelection).toHaveBeenCalled();
  });

  it('should call setActiveTab and updateDropdownSelection on onTabChange', () => {
    const event = { target: { value: 'description' } } as unknown as Event;
    spyOn(component, 'setActiveTab');
    spyOn(component, 'updateDropdownSelection');

    component.onTabChange(event);

    expect(component.setActiveTab).toHaveBeenCalledWith('description');
  });

  it('should return true for description when it is not null and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: {en: 'Test description'}
    };

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return false for description when it is null or empty', () => {
    const mockContentWithEmptyDescription: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithEmptyDescription);
    expect(component.getContent('description')).toBeFalse();

    const mockContentWithNullDescription: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };
    component.productModuleContent.set(mockContentWithNullDescription);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return true for setup when it is not null and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: 'Test setup'
    };

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return false for setup when it is null or empty', () => {
    const mockContentWithEmptySetup: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithEmptySetup);
    expect(component.getContent('setup')).toBeFalse();

    const mockContentWithNullSetup: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };
    component.productModuleContent.set(mockContentWithNullSetup);
    expect(component.getContent('setup')).toBeFalse();
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
  
  it('should call checkMediaSize on ngAfterViewInit', fakeAsync(() => {
    spyOn(component, 'checkMediaSize');
    component.ngAfterViewInit();
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
});
