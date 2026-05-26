import { inject, Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DeleteReleaseLetterConfirmModalComponent } from '../../modules/admin-dashboard/news-management/delete-release-letter-confirm-modal/delete-release-letter-confirm-modal.component';
import { ReleaseLetterModalComponent } from '../../modules/admin-dashboard/news-management/release-letter-modal/release-letter-modal.component';
import { AddFeedbackDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/add-feedback-dialog.component';
import { SuccessDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/success-dialog/success-dialog.component';
import { ShowFeedbacksDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/show-feedbacks-dialog/show-feedbacks-dialog.component';
import { ReleaseLetter } from '../models/release-letter-request.model';

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

  openReleaseLetterModal(id: string): void {
    const modalRef = this.modalService.open(ReleaseLetterModalComponent, {
      fullscreen: 'md',
      centered: true,
      size: 'xl'
    });

    modalRef.componentInstance.id = id;
  }

  openDeleteReleaseLetterConfirmModal(releaseLetter: ReleaseLetter) {
    const modalRef = this.modalService.open(
      DeleteReleaseLetterConfirmModalComponent,
      {
        size: 'lg'
      }
    );

    modalRef.componentInstance.id = releaseLetter.id;
    modalRef.componentInstance.sprint = releaseLetter.sprint;

    return modalRef.result;
  }
}
