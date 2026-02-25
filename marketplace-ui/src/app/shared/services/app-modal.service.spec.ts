import { TestBed } from '@angular/core/testing';

import { AppModalService } from './app-modal.service';
import { AddFeedbackDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/add-feedback-dialog.component';
import { SuccessDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/product-star-rating-panel/add-feedback-dialog/success-dialog/success-dialog.component';
import { ShowFeedbacksDialogComponent } from '../../modules/product/product-detail/product-detail-feedback/show-feedbacks-dialog/show-feedbacks-dialog.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ReleaseLetterModalComponent } from '../../modules/admin-dashboard/news-management/release-letter-modal/release-letter-modal.component';
import { DeleteReleaseLetterConfirmModalComponent } from '../../modules/admin-dashboard/news-management/delete-release-letter-confirm-modal/delete-release-letter-confirm-modal.component';

describe('AppModalService', () => {
  let service: AppModalService;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('NgbModal', ['open']);

    TestBed.configureTestingModule({
      providers: [
        AppModalService,
        { provide: NgbModal, useValue: spy }
      ]
    });

    service = TestBed.inject(AppModalService);
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  it('should open ShowFeedbacksDialogComponent with correct options', () => {
    service.openShowFeedbacksDialog();
    expect(modalServiceSpy.open).toHaveBeenCalledWith(ShowFeedbacksDialogComponent, {
      centered: true,
      modalDialogClass: 'show-feedbacks-modal-dialog',
      windowClass: 'overflow-hidden'
    });
  });

  it('should open AddFeedbackDialogComponent with correct options and return result', async () => {
    const mockResult = Promise.resolve('test result');
    modalServiceSpy.open.and.returnValue({ result: mockResult } as any);

    const result = await service.openAddFeedbackDialog();
    expect(modalServiceSpy.open).toHaveBeenCalledWith(AddFeedbackDialogComponent, {
      fullscreen: 'md',
      centered: true,
      modalDialogClass: 'add-feedback-modal-dialog'
    });
    expect(result).toBe('test result');
  });

  it('should open SuccessDialogComponent with correct options', () => {
    service.openSuccessDialog();
    expect(modalServiceSpy.open).toHaveBeenCalledWith(SuccessDialogComponent, {
      fullscreen: 'md',
      centered: true,
      modalDialogClass: 'add-feedback-modal-dialog'
    });
  });

  it('should open ReleaseLetterModalComponent and set item on componentInstance', () => {
    const mockItem = { sprint: 'S43' } as any;

    const mockModalRef = {
      componentInstance: {}
    } as any;

    modalServiceSpy.open.and.returnValue(mockModalRef);

    service.openReleaseLetterModal(mockItem);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(
      ReleaseLetterModalComponent,
      {
        fullscreen: 'md',
        centered: true,
        size: 'xl'
      }
    );

    expect(mockModalRef.componentInstance.item).toBe(mockItem);
  });

  it('should open DeleteReleaseLetterConfirmModalComponent, set sprint, and return result', async () => {
    const sprint = 'S43';
    const mockResult = Promise.resolve(true);

    const mockModalRef = {
      componentInstance: {},
      result: mockResult
    } as any;

    modalServiceSpy.open.and.returnValue(mockModalRef);

    const result = await service.openDeleteReleaseLetterConfirmModal(sprint);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(
      DeleteReleaseLetterConfirmModalComponent,
      {
        size: 'lg'
      }
    );

    expect(mockModalRef.componentInstance.releaseLetterSprint).toBe(sprint);
    expect(result).toBe(true);
  });
});
