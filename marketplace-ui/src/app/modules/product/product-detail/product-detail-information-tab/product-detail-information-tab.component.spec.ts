import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductDetailInformationTabComponent } from './product-detail-information-tab.component';
import { MOCK_PRODUCT_DETAIL } from '../../../../shared/mocks/mock-data';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

describe('InformationDetailComponent', () => {
  let component: ProductDetailInformationTabComponent;
  let fixture: ComponentFixture<ProductDetailInformationTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProductDetailInformationTabComponent,
        TranslateModule.forRoot()
      ],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetailInformationTabComponent);
    component = fixture.componentInstance;
    component.productDetail = MOCK_PRODUCT_DETAIL;
    component.selectedVersion = '1.0.0';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
