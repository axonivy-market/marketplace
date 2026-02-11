import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteReleaseLetterConfirmModalComponent } from './delete-release-letter-confirm-modal.component';

describe('DeleteReleaseLetterConfirmModalComponent', () => {
  let component: DeleteReleaseLetterConfirmModalComponent;
  let fixture: ComponentFixture<DeleteReleaseLetterConfirmModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteReleaseLetterConfirmModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeleteReleaseLetterConfirmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
