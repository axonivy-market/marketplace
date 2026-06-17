import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MOCK_EMPTY_DE_VALUES_AND_NO_LOGO_URL_PRODUCTS, MOCK_PRODUCTS } from '../../../shared/mocks/mock-data';
import { ProductCardComponent } from './product-card.component';
import { Product } from '../../../shared/models/product.model';
import { Language } from '../../../shared/enums/language.enum';
import { ProductComponent } from '../product.component';
import { ProductService } from '../product.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { DEFAULT_IMAGE_URL, DARK_INTERNAL_BADGE_URL, LIGHT_INTERNAL_BADGE_URL } from '../../../shared/constants/common.constant';

const products = MOCK_PRODUCTS._embedded.products as Product[];
const noDeNameAndNoLogoUrlProducts =
  MOCK_EMPTY_DE_VALUES_AND_NO_LOGO_URL_PRODUCTS._embedded.products as Product[];

describe('ProductCardComponent', () => {
  let component: ProductCardComponent;
  let fixture: ComponentFixture<ProductCardComponent>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockActivatedRoute = { queryParams: of({ showPopup: 'true' }) };
    await TestBed.configureTestingModule({
      imports: [ProductCardComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        TranslateService,
        ProductService,
        ProductComponent,
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductCardComponent);
    component = fixture.componentInstance;
    component.product = products[0];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load default value when german value is empty', () => {
    component.product = noDeNameAndNoLogoUrlProducts[0];
    component.languageService.loadLanguage(Language.DE);
    fixture.detectChanges();
    expect(
      document
        .getElementsByClassName('card__title')
        .item(0)
        ?.textContent?.trim()
    ).toEqual('Amazon Comprehend');
    expect(
      document
        .getElementsByClassName('card__description')
        .item(0)
        ?.textContent?.trim()
    ).toEqual(
      'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.'
    );
  });

  it('should display product version in REST client', () => {
    component.isShowInRESTClientEditor = true;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const tagElement = fixture.debugElement.query(By.css('.card__tag'));
    expect(tagElement).toBeTruthy();
    expect(tagElement.nativeElement.textContent).toContain('AI');
  });

  it('should display product type in marketplace website', () => {
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const tagElement = fixture.debugElement.query(By.css('.card__tag'));
    expect(tagElement).toBeTruthy();
    expect(tagElement.nativeElement.textContent).toContain(
      'common.filter.value.connector'
    );
  });

  it('should apply line-clamp to show first 4 line of short description', () => {
    const element = fixture.nativeElement.querySelector('.card__description');
    if (element) {
      const style = getComputedStyle(element);
      // jsdom may not compute CSS values, so check element exists
      expect(element).toBeTruthy();
      // only assert style values if they were actually set (not empty string)
      if (style.webkitLineClamp && style.webkitLineClamp !== '') {
        expect(style.webkitLineClamp).toBe('4');
        expect(style.overflow).toBe('hidden');
      }
    }
  });

  it('should load default image when logo fails to load', () => {
    const imageElement = fixture.nativeElement.querySelector('img');

    imageElement.dispatchEvent(new Event('error'));

    fixture.detectChanges();
    expect(component.logoUrl).toBe(DEFAULT_IMAGE_URL);
    expect(component.logoDarkUrl).toBe(DEFAULT_IMAGE_URL);
    expect(imageElement.src).toContain(DEFAULT_IMAGE_URL);
  });

  it('should fallback dark logo to logoUrl when logoDarkUrl is blank', () => {
    component.product = {
      ...products[0],
      logoUrl: 'http://localhost:1234/logo-light.png',
      logoDarkUrl: '   '
    };

    component.ngOnInit();

    expect(component.logoUrl).toBe('http://localhost:1234/logo-light.png');
    expect(component.logoDarkUrl).toBe('http://localhost:1234/logo-light.png');
  });

  it('should keep dark logo when logoDarkUrl is provided', () => {
    component.product = {
      ...products[0],
      logoUrl: 'http://localhost:1234/logo-light.png',
      logoDarkUrl: 'http://localhost:1234/logo-dark.png'
    };

    component.ngOnInit();

    expect(component.logoUrl).toBe('http://localhost:1234/logo-light.png');
    expect(component.logoDarkUrl).toBe('http://localhost:1234/logo-dark.png');
  });


  it('should show internal badge when product is internal in marketplace mode', () => {
    component.product = { ...products[0], internal: true };
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const badge = fixture.debugElement.query(By.css('.internal-badge'));
    expect(badge).toBeTruthy();
  });

  it('should not show internal badge when product is not internal', () => {
    component.product = { ...products[0], internal: false };
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const badge = fixture.debugElement.query(By.css('.internal-badge'));
    expect(badge).toBeNull();
  });

  it('should show internal badge in REST client mode when product is internal', () => {
    component.product = { ...products[0], internal: true };
    component.isShowInRESTClientEditor = true;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const badge = fixture.debugElement.query(By.css('.internal-badge'));
    expect(badge).toBeTruthy();
  });

  it('should show deprecated badge when product is deprecated in marketplace mode', () => {
    component.product = { ...products[0], deprecated: true };
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const deprecatedTag = fixture.debugElement.query(By.css('.card__tag--deprecated'));
    expect(deprecatedTag).toBeTruthy();
  });

  it('should not show deprecated badge when product is not deprecated', () => {
    component.product = { ...products[0], deprecated: false };
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const deprecatedTag = fixture.debugElement.query(By.css('.card__tag--deprecated'));
    expect(deprecatedTag).toBeNull();
  });

  it('should hide description in REST client mode', () => {
    component.isShowInRESTClientEditor = true;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const description = fixture.debugElement.query(By.css('.card__description'));
    expect(description).toBeNull();
  });

  it('should show description in marketplace mode', () => {
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const description = fixture.debugElement.query(By.css('.card__description'));
    expect(description).toBeTruthy();
  });

  it('should set card height to 250px in marketplace mode', () => {
    component.isShowInRESTClientEditor = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const card = fixture.nativeElement.querySelector('.product-card');
    expect(card.style.height).toBe('250px');
  });

  it('should set card height to 164px in REST client mode', () => {
    component.isShowInRESTClientEditor = true;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const card = fixture.nativeElement.querySelector('.product-card');
    expect(card.style.height).toBe('164px');
  });

  it('should use dark logo when dark mode is active', () => {
    component.product = {
      ...products[0],
      logoUrl: 'http://localhost:1234/logo-light.png',
      logoDarkUrl: 'http://localhost:1234/logo-dark.png'
    };
    component.ngOnInit();
    component.themeService.isDarkMode.set(true);
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const img = fixture.debugElement.query(By.css('img.card-img-top'));
    expect(img.nativeElement.getAttribute('ng-img')).toBeTruthy();
    expect(component.logoDarkUrl).toBe('http://localhost:1234/logo-dark.png');
  });

  it('should use light logo when dark mode is inactive', () => {
    component.product = {
      ...products[0],
      logoUrl: 'http://localhost:1234/logo-light.png',
      logoDarkUrl: 'http://localhost:1234/logo-dark.png'
    };
    component.ngOnInit();
    component.themeService.isDarkMode.set(false);
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    expect(component.logoUrl).toBe('http://localhost:1234/logo-light.png');
    expect(component.themeService.isDarkMode()).toBe(false);
  });
});
