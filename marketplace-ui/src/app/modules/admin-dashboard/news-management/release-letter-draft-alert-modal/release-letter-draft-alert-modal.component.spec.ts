import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleaseLetterDraftAlertModalComponent } from './release-letter-draft-alert-modal.component';

describe('ReleaseLetterDraftAlertModalComponent', () => {
  let component: ReleaseLetterDraftAlertModalComponent;
  let fixture: ComponentFixture<ReleaseLetterDraftAlertModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReleaseLetterDraftAlertModalComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterDraftAlertModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
