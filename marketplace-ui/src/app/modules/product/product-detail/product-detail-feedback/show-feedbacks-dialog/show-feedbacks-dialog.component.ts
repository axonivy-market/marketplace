import { Component, HostListener, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ProductFeedbacksPanelComponent } from '../product-feedbacks-panel/product-feedbacks-panel.component';
import { ProductStarRatingPanelComponent } from '../product-star-rating-panel/product-star-rating-panel.component';
import { AppModalService } from '../../../../../shared/services/app-modal.service';

@Component({
  selector: 'app-show-feedbacks-dialog',
  imports: [ProductFeedbacksPanelComponent, ProductStarRatingPanelComponent],
  templateUrl: './show-feedbacks-dialog.component.html',
  styleUrl: './show-feedbacks-dialog.component.scss'
})
export class ShowFeedbacksDialogComponent {
  activeModal = inject(NgbActiveModal);
  appModalService = inject(AppModalService);

  @HostListener('window:resize', ['$event'])
  onResize() {
    const mediaQuery = window.matchMedia('(max-width: 767px)');
    if (mediaQuery.matches) {
      this.activeModal.dismiss();
    }
  }
}
