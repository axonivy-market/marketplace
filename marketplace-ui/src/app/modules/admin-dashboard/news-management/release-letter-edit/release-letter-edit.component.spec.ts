import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleaseLetterEditComponent } from './release-letter-edit.component';

describe('ReleaseLetterEditComponent', () => {
  let component: ReleaseLetterEditComponent;
  let fixture: ComponentFixture<ReleaseLetterEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReleaseLetterEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
