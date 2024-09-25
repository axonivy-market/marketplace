import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalDocumentComponent } from './external-document.component';

describe('ExternalDocumentComponent', () => {
  let component: ExternalDocumentComponent;
  let fixture: ComponentFixture<ExternalDocumentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExternalDocumentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExternalDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
