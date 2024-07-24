import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { ProductStarRatingPanelComponent } from './product-star-rating-panel.component';
import { ProductStarRatingService } from './product-star-rating.service';
import { ProductStarRatingNumberComponent } from '../../product-star-rating-number/product-star-rating-number.component';
import { StarRatingHighlightDirective } from './star-rating-highlight.directive';
import { StarRatingCounting } from '../../../../../shared/models/star-rating-counting.model';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';

describe('ProductStarRatingPanelComponent', () => {
  let component: ProductStarRatingPanelComponent;
  let fixture: ComponentFixture<ProductStarRatingPanelComponent>;
  let productStarRatingServiceMock: jasmine.SpyObj<ProductStarRatingService>;

  beforeEach(async () => {
    const productStarRatingServiceSpy = jasmine.createSpyObj(
      'ProductStarRatingService',
      [],
      { 
        reviewNumber: signal(0),
        totalComments: signal(0),
        starRatings: signal([] as StarRatingCounting[])
      }
    );

    await TestBed.configureTestingModule({
      imports: [
        ProductStarRatingPanelComponent,
        CommonModule,
        TranslateModule.forRoot(),
        ProductStarRatingNumberComponent,
        StarRatingHighlightDirective
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: ProductStarRatingService,
          useValue: productStarRatingServiceSpy
        }
      ]
    }).compileComponents();

    productStarRatingServiceMock = TestBed.inject(
      ProductStarRatingService
    ) as jasmine.SpyObj<ProductStarRatingService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductStarRatingPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit openAddFeedbackDialog event when child component triggers the event', () => {
    spyOn(component.openAddFeedbackDialog, 'emit');
    const starRatingNumberComponent =
      fixture.debugElement.nativeElement.querySelector(
        'app-product-star-rating-number'
      );
    starRatingNumberComponent.dispatchEvent(new Event('openAddFeedbackDialog'));

    expect(component.openAddFeedbackDialog.emit).toHaveBeenCalled();
  });

  it('should render star ratings correctly', () => {
    const mockStarRatings: StarRatingCounting[] = [
      { starRating: 5, percent: 80 },
      { starRating: 4, percent: 15 },
      { starRating: 3, percent: 5 }
    ];
    productStarRatingServiceMock.starRatings.set(mockStarRatings);

    fixture.detectChanges();

    const starRatingElements = fixture.nativeElement.querySelectorAll(
      '.start-rating-counting-line'
    );
    expect(starRatingElements.length).toBe(mockStarRatings.length);

    mockStarRatings.forEach((rating, index) => {
      const starRatingElement = starRatingElements[index];
      expect(
        starRatingElement
          .querySelector('.number-star-rating')
          .textContent.trim()
      ).toBe(rating.starRating.toString());
      expect(
        starRatingElement.querySelector('.star-rating-percent').style.width
      ).toBe(`${rating.percent}%`);
    });
  });
});
