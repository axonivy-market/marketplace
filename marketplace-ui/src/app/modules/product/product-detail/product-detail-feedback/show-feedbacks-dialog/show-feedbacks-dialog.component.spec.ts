import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ShowFeedbacksDialogComponent } from './show-feedbacks-dialog.component';
import { ProductFeedbacksPanelComponent } from '../product-feedbacks-panel/product-feedbacks-panel.component';
import { ProductStarRatingPanelComponent } from '../product-star-rating-panel/product-star-rating-panel.component';
import { AppModalService } from '../../../../../shared/services/app-modal.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('ShowFeedbacksDialogComponent', () => {
  let component: ShowFeedbacksDialogComponent;
  let fixture: ComponentFixture<ShowFeedbacksDialogComponent>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;
  let mockAppModalService: jasmine.SpyObj<AppModalService>;

  beforeEach(async () => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['dismiss']);
    mockAppModalService = jasmine.createSpyObj('AppModalService', [
      'openAddFeedbackDialog'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        ShowFeedbacksDialogComponent,
        CommonModule,
        ProductFeedbacksPanelComponent,
        ProductStarRatingPanelComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        TranslateService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: NgbActiveModal, useValue: mockActiveModal },
        { provide: AppModalService, useValue: mockAppModalService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowFeedbacksDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render ProductStarRatingPanelComponent and ProductFeedbacksPanelComponent', () => {
    const starRatingPanel = fixture.debugElement.query(
      By.directive(ProductStarRatingPanelComponent)
    );
    const feedbacksPanel = fixture.debugElement.query(
      By.directive(ProductFeedbacksPanelComponent)
    );

    expect(starRatingPanel).toBeTruthy();
    expect(feedbacksPanel).toBeTruthy();
  });

  it('should call openAddFeedbackDialog on ProductStarRatingPanelComponent event', () => {
    const starRatingPanel = fixture.debugElement.query(
      By.directive(ProductStarRatingPanelComponent)
    );
    starRatingPanel.triggerEventHandler('openAddFeedbackDialog', null);
    fixture.detectChanges();

    expect(mockAppModalService.openAddFeedbackDialog).toHaveBeenCalled();
  });

  it('should call activeModal.dismiss when the window is resized to less than or equal to 767px', () => {
    // Set up a media query list that matches
    spyOn(window, 'matchMedia').and.returnValue({
      matches: true,
      addListener: () => {
      },
      removeListener: () => {
      }
    } as any);

    // Trigger the resize event
    component.onResize();

    // Check that dismiss was called
    expect(mockActiveModal.dismiss).toHaveBeenCalled();
  });

  it('should not call activeModal.dismiss when the window is resized to more than 767px', () => {
    // Set up a media query list that does not match
    spyOn(window, 'matchMedia').and.returnValue({
      matches: false,
      addListener: () => {
      },
      removeListener: () => {
      }
    } as any);

    // Trigger the resize event
    component.onResize();

    // Check that dismiss was not called
    expect(mockActiveModal.dismiss).not.toHaveBeenCalled();
  });

});
