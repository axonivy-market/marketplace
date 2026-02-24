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
      'deleteReleaseLetterBySprint'
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

  it('should call deleteReleaseLetterBySprint and close modal on success', () => {
    component.releaseLetterSprint = 'S42';

    adminDashboardServiceMock.deleteReleaseLetterBySprint.and.returnValue(
      of(void 0)
    );

    component.deleteReleaseLetterBySprint();

    expect(
      adminDashboardServiceMock.deleteReleaseLetterBySprint
    ).toHaveBeenCalledWith('S42');

    expect(activeModalMock.close).toHaveBeenCalled();
  });

  it('should not close modal before observable emits', () => {
    component.releaseLetterSprint = 'S42';

    const subject = new Subject<void>();
    adminDashboardServiceMock.deleteReleaseLetterBySprint.and.returnValue(
      subject.asObservable()
    );

    component.deleteReleaseLetterBySprint();

    expect(activeModalMock.close).not.toHaveBeenCalled();

    subject.next();
    subject.complete();

    expect(activeModalMock.close).toHaveBeenCalled();
  });
});
