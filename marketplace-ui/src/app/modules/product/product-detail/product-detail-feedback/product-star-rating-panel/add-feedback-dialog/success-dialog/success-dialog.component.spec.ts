import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SuccessDialogComponent } from './success-dialog.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../../../../auth/auth.service';
import { NgOptimizedImage } from '@angular/common';

describe('SuccessDialogComponent', () => {
  let component: SuccessDialogComponent;
  let fixture: ComponentFixture<SuccessDialogComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getDisplayName']);

    await TestBed.configureTestingModule({
      imports: [SuccessDialogComponent, TranslateModule.forRoot(), NgOptimizedImage],
      providers: [
        NgbActiveModal,
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SuccessDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display translated thank message with displayName', () => {
    const mockDisplayName = 'John Doe';
    mockAuthService.getDisplayName.and.returnValue(mockDisplayName);
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const thankMessageElement = compiled.querySelector('.modal-body h4.text-primary:last-child');
    expect(thankMessageElement).toBeTruthy();
    expect(thankMessageElement.textContent.trim()).toContain(`common.feedback.thankMessage ${mockDisplayName}`);
  });

  it('should dismiss modal when close button is clicked', () => {
    const dismissSpy = spyOn(component.activeModal, 'dismiss');
    const closeButton = fixture.nativeElement.querySelector('.btn-close');
    closeButton.click();
    expect(dismissSpy).toHaveBeenCalled();
  });
});
