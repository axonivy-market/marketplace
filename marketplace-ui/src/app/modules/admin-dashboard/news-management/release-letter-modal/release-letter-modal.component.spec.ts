import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleaseLetterModalComponent } from './release-letter-modal.component';

describe('ReleaseLetterModalComponent', () => {
  let component: ReleaseLetterModalComponent;
  let fixture: ComponentFixture<ReleaseLetterModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReleaseLetterModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
