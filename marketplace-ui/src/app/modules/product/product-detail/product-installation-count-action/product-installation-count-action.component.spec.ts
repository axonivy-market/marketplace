import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ProductInstallationCountActionComponent } from './product-installation-count-action.component';
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ProductService } from '../../product.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('ProductInstallationCountActionComponent', () => {
  let component: ProductInstallationCountActionComponent;
  let fixture: ComponentFixture<ProductInstallationCountActionComponent>;
  let productServiceMock: jasmine.SpyObj<ProductService>;

  beforeEach(() => {
    productServiceMock = jasmine.createSpyObj('ProductService', [
      'sendRequestToGetInstallationCount'
    ]);

    TestBed.configureTestingModule({
      imports: [
        ProductInstallationCountActionComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        TranslateService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ProductService, useValue: productServiceMock }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ProductInstallationCountActionComponent);
    component = fixture.componentInstance;
  });


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update installation count when refreshInstallationCount changes', fakeAsync(() => {
    const refreshSignal = signal(0);
    const productId = 'portal';
    component.productId = productId;
    component.refreshInstallationCount = refreshSignal;
    productServiceMock.sendRequestToGetInstallationCount.and.returnValue(of(42));

    fixture.detectChanges();
    refreshSignal.update(v => v + 1);
    tick(1000);

    expect(productServiceMock.sendRequestToGetInstallationCount).toHaveBeenCalledWith(productId);
    expect(component.currentInstallationCount()).toEqual(42);
  }));
});
