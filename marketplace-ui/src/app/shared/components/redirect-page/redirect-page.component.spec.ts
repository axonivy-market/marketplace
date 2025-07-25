import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ROUTER } from '../../constants/router.constant';
import { RedirectPageComponent } from './redirect-page.component';
import { MOCK_STATIC_LIB } from '../../mocks/mock-data';
import { ProductService } from '../../../modules/product/product.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { of } from 'rxjs';

describe('RedirectPageComponent', () => {
  let component: RedirectPageComponent;
  let fixture: any;
  let mockProductService: jasmine.SpyObj<ProductService>;

  const mockActivatedRoute = {
    snapshot: {
      paramMap: {
        get: (key: string) => {
          if (key === ROUTER.ID) return 'connectivity-demo';
          if (key === ROUTER.VERSION) return 'dev';
          if (key === ROUTER.ARTIFACT) return 'connectivity-demos';
          return null;
        }
      }
    }
  };

  beforeEach(() => {
    mockProductService = jasmine.createSpyObj('ProductService', ['getLatestArtifactDownloadUrl']);

    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), RedirectPageComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: ProductService, useValue: mockProductService }
      ]
    });

    fixture = TestBed.createComponent(RedirectPageComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to latest Lib version download url', () => {
    mockProductService.getLatestArtifactDownloadUrl.and.returnValue(of(MOCK_STATIC_LIB.relativeLink));
    fixture.detectChanges();
  });
});