import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoogleSearchComponentComponent } from './google-search-component.component';

describe('GoogleSearchComponentComponent', () => {
  let component: GoogleSearchComponentComponent;
  let fixture: ComponentFixture<GoogleSearchComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GoogleSearchComponentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GoogleSearchComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
