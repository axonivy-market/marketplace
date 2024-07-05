import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { MOCK_PRODUCTS } from '../../../shared/mocks/mock-data';
import { MockProductService } from '../../../shared/mocks/mock-services';
import { ProductService } from '../product.service';
import { ProductDetailComponent } from './product-detail.component';
import { TranslateModule } from '@ngx-translate/core';
import { Product } from '../../../shared/models/product.model';

const products = MOCK_PRODUCTS._embedded.products as Product[];

describe('ProductDetailComponent', () => {
  let component: ProductDetailComponent;
  let fixture: ComponentFixture<ProductDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailComponent, TranslateModule.forRoot()],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: products[0].id }
            }
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
    expect(component.product.names.en).toEqual(products[0].names.en);
  });
});
