import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductDetailMavenContentComponent } from './product-detail-maven-content.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MOCK_PRODUCT_DETAILS } from '../../../../shared/mocks/mock-data';

describe('ProductDetailMavenContentComponent', () => {
  let component: ProductDetailMavenContentComponent;
  let fixture: ComponentFixture<ProductDetailMavenContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailMavenContentComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetailMavenContentComponent);
    component = fixture.componentInstance;
    component.productModuleContent = MOCK_PRODUCT_DETAILS.productModuleContent;
    component.selectedVersion = '1.0.0';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
