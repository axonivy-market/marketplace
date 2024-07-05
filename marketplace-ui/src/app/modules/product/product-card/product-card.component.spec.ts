import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  MOCK_EMPTY_DE_VALUES_AND_NO_LOGO_URL_PRODUCTS,
  MOCK_PRODUCTS
} from '../../../shared/mocks/mock-data';
import { ProductCardComponent } from './product-card.component';
import { Product } from '../../../shared/models/product.model';
import { Language } from '../../../shared/enums/language.enum';

const products = MOCK_PRODUCTS._embedded.products as Product[];
const noDeNameAndNoLogoUrlProducts =
  MOCK_EMPTY_DE_VALUES_AND_NO_LOGO_URL_PRODUCTS._embedded.products as Product[];

describe('ProductCardComponent', () => {
  let component: ProductCardComponent;
  let fixture: ComponentFixture<ProductCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductCardComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
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
});
