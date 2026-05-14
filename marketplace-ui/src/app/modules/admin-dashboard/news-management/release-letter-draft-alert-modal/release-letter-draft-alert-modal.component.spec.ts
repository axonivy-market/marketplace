import { beforeEach, describe, expect, it, MockedObject, vi } from 'vitest';
import { ReleaseLetterDraftAlertModalComponent } from './release-letter-draft-alert-modal.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewsManagementService } from '../news-management.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';

describe('ReleaseLetterDraftAlertModalComponent', () => {
  let component: ReleaseLetterDraftAlertModalComponent;
  let fixture: ComponentFixture<ReleaseLetterDraftAlertModalComponent>;
  let newsManagementServiceMock: MockedObject<NewsManagementService>;
  let activeModalMock: MockedObject<NgbActiveModal>;

  beforeEach(async () => {
    newsManagementServiceMock = {
      deleteReleaseLetterById: vi.fn().mockName('NewsManagementService.deleteReleaseLetterById')
    } as MockedObject<NewsManagementService>;

    activeModalMock = {
      close: vi.fn().mockName('NgbActiveModal.close'),
      dismiss: vi.fn().mockName('NgbActiveModal.dismiss'),
      update: vi.fn().mockName('NgbActiveModal.update')
    } as MockedObject<NgbActiveModal>;

    await TestBed.configureTestingModule({
      imports: [ReleaseLetterDraftAlertModalComponent, TranslateModule.forRoot()],
      providers: [
        { provide: NewsManagementService, useValue: newsManagementServiceMock },
        { provide: NgbActiveModal, useValue: activeModalMock },
        TranslateService,
        ThemeService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterDraftAlertModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should close modal with true when onConfirm is called', () => {
    component.onConfirm();

    expect(activeModalMock.close).toHaveBeenCalledTimes(1);
    expect(activeModalMock.close).toHaveBeenCalledWith(true);
  });

  it('should close modal with false when onCancel is called', () => {
    component.onCancel();

    expect(activeModalMock.close).toHaveBeenCalledTimes(1);
    expect(activeModalMock.close).toHaveBeenCalledWith(false);
  });
});
