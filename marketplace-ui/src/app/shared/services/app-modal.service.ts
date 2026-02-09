import { inject, Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ShowFeedbacksDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/show-feedbacks-dialog/show-feedbacks-dialog.component';
import { AddFeedbackDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/add-feedback-dialog.component';
import { SuccessDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/success-dialog/success-dialog.component';
import { ReleaseLetterModalComponent } from '../../modules/admin-dashboard/news-management/release-letter-modal/release-letter-modal.component';

@Injectable({
  providedIn: 'root'
})
export class AppModalService {
  private readonly modalService = inject(NgbModal);

  openReleaseLetterModal(item: any): void {
    const modalRef = this.modalService.open(ReleaseLetterModalComponent, {
      fullscreen: 'md',
      centered: true,
      size: 'xl'
    });

    modalRef.componentInstance.item = item;
  }

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
}
