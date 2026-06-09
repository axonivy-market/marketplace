import { NewsManagementService } from './../news-management.service';
import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { DeleteReleaseLetterConfirmModalComponent } from './delete-release-letter-confirm-modal.component';

describe('DeleteReleaseLetterConfirmModalComponent', () => {
  let component: DeleteReleaseLetterConfirmModalComponent;
  let fixture: ComponentFixture<DeleteReleaseLetterConfirmModalComponent>;
  let newsManagementServiceMock: MockedObject<NewsManagementService>;
  let activeModalMock: MockedObject<NgbActiveModal>;

  beforeEach(async () => {
    newsManagementServiceMock = {
      deleteReleaseLetterById: vi
        .fn()
        .mockName('NewsManagementService.deleteReleaseLetterById')
    } as MockedObject<NewsManagementService>;

    activeModalMock = {
      close: vi.fn().mockName('NgbActiveModal.close'),
      dismiss: vi.fn().mockName('NgbActiveModal.dismiss'),
      update: vi.fn().mockName('NgbActiveModal.update')
    } as MockedObject<NgbActiveModal>;

    await TestBed.configureTestingModule({
      imports: [
        DeleteReleaseLetterConfirmModalComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: NewsManagementService, useValue: newsManagementServiceMock },
        { provide: NgbActiveModal, useValue: activeModalMock },
        TranslateService,
        ThemeService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteReleaseLetterConfirmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call deleteReleaseLetterById and close modal on success', () => {
    component.id = '123';

    newsManagementServiceMock.deleteReleaseLetterById.mockReturnValue(
      of(void 0)
    );

    component.deleteReleaseLetterById();

    expect(
      newsManagementServiceMock.deleteReleaseLetterById
    ).toHaveBeenCalledWith('123');

    expect(activeModalMock.close).toHaveBeenCalled();
  });

  it('should not close modal before observable emits', () => {
    component.id = '123';

    const subject = new Subject<void>();
    newsManagementServiceMock.deleteReleaseLetterById.mockReturnValue(
      subject.asObservable()
    );

    component.deleteReleaseLetterById();

    expect(activeModalMock.close).not.toHaveBeenCalled();

    subject.next();
    subject.complete();

    expect(activeModalMock.close).toHaveBeenCalled();
  });
});
