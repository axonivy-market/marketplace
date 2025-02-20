import { inject, Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ShowFeedbacksDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/show-feedbacks-dialog/show-feedbacks-dialog.component';
import { AddFeedbackDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/add-feedback-dialog.component';
import { SuccessDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/success-dialog/success-dialog.component';
import { FeedbackReviewComponent } from '../../modules/feedback-review/feedback-review.component';

@Injectable({
  providedIn: 'root'
})
export class AppModalService {
  private readonly modalService = inject(NgbModal);

  openShowFeedbacksDialog(): void {
    this.modalService.open(ShowFeedbacksDialogComponent, {
      centered: true,
      modalDialogClass: 'show-feedbacks-modal-dialog',
      windowClass: 'overflow-hidden'
    });
  }

  openAddFeedbackDialog() {
    const addFeedbackDialog = this.modalService.open(
      AddFeedbackDialogComponent,
      {
        fullscreen: 'md',
        centered: true,
        modalDialogClass: 'add-feedback-modal-dialog'
      }
    );
    return addFeedbackDialog.result;
  }

  openSuccessDialog(): void {
    this.modalService.open(SuccessDialogComponent, {
      fullscreen: 'md',
      centered: true,
      modalDialogClass: 'add-feedback-modal-dialog'
    });
  }

  openFeedbacksDialog() {
this.modalService.open(
  FeedbackReviewComponent,
      {
        fullscreen: 'md',
        centered: true,
        modalDialogClass: 'app-feedback-review'
      }
    );
  }
}
