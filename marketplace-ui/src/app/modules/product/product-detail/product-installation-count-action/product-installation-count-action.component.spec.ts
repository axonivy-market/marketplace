import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { ProductInstallationCountActionComponent } from './product-installation-count-action.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product.service';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('ProductInstallationCountActionComponent', () => {
  let component: ProductInstallationCountActionComponent;
  let fixture: ComponentFixture<ProductInstallationCountActionComponent>;
  let productServiceMock: MockedObject<ProductService>;

  beforeEach(() => {
    productServiceMock = {
      sendRequestToGetInstallationCount: vi
        .fn()
        .mockName('ProductService.sendRequestToGetInstallationCount')
    } as unknown as MockedObject<ProductService>;

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

  it('should update installation count when refreshInstallationCount changes', async () => {
    vi.useFakeTimers();
    const refreshSignal = signal(0);
    const productId = 'portal';
    component.productId = productId;
    component.refreshInstallationCount = refreshSignal;
    productServiceMock.sendRequestToGetInstallationCount.mockReturnValue(
      of(42)
    );

    fixture.detectChanges();
    refreshSignal.update(v => v + 1);
    fixture.detectChanges();
    await vi.advanceTimersByTimeAsync(1000);

    expect(
      productServiceMock.sendRequestToGetInstallationCount
    ).toHaveBeenCalledWith(productId);
    expect(component.currentInstallationCount()).toEqual(42);
    vi.useRealTimers();
  });
});
