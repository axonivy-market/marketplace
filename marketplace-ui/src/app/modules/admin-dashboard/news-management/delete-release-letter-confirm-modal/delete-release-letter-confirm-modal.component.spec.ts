import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { AdminDashboardService } from '../../admin-dashboard.service';
import { DeleteReleaseLetterConfirmModalComponent } from './delete-release-letter-confirm-modal.component';

describe('DeleteReleaseLetterConfirmModalComponent', () => {
  let component: DeleteReleaseLetterConfirmModalComponent;
  let fixture: ComponentFixture<DeleteReleaseLetterConfirmModalComponent>;

  let adminDashboardServiceMock: jasmine.SpyObj<AdminDashboardService>;
  let activeModalMock: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    adminDashboardServiceMock = jasmine.createSpyObj('AdminDashboardService', [
      'deleteReleaseLetterById'
    ]);

    activeModalMock = jasmine.createSpyObj('NgbActiveModal', [
      'close',
      'dismiss'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        DeleteReleaseLetterConfirmModalComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: AdminDashboardService, useValue: adminDashboardServiceMock },
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

    adminDashboardServiceMock.deleteReleaseLetterById.and.returnValue(
      of(void 0)
    );

    component.deleteReleaseLetterById();

    expect(
      adminDashboardServiceMock.deleteReleaseLetterById
    ).toHaveBeenCalledWith('123');

    expect(activeModalMock.close).toHaveBeenCalled();
  });

  it('should not close modal before observable emits', () => {
    component.id = '123';

    const subject = new Subject<void>();
    adminDashboardServiceMock.deleteReleaseLetterById.and.returnValue(
      subject.asObservable()
    );

    component.deleteReleaseLetterById();

    expect(activeModalMock.close).not.toHaveBeenCalled();

    subject.next();
    subject.complete();

    expect(activeModalMock.close).toHaveBeenCalled();
  });
});
