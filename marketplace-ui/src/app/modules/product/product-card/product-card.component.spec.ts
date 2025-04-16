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
    fixture.detectChanges();

    const tagElement = fixture.debugElement.query(By.css('.card__tag'));
    expect(tagElement).toBeTruthy();
    expect(tagElement.nativeElement.textContent).toContain('AI');
  });

  it('should display product type in marketplace website', () => {
    component.isShowInRESTClientEditor = false;
    fixture.detectChanges();

    const tagElement = fixture.debugElement.query(By.css('.card__tag'));
    expect(tagElement).toBeTruthy();
    expect(tagElement.nativeElement.textContent).toContain(
      'common.filter.value.connector'
    );
  });

  it('should apply line-clamp to show first 4 line of short description', () => {
    const element = fixture.nativeElement.querySelector('.card__description');
    const style = getComputedStyle(element);
    expect(style.display).toBe('-webkit-box');
    expect(style.webkitLineClamp).toBe('4');
  });
});
