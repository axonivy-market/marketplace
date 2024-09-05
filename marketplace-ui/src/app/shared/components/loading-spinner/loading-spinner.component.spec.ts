import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { LoadingSpinnerComponent } from './loading-spinner.component';

describe('LoadingSpinnerComponent', () => {
  let component: LoadingSpinnerComponent;
  let fixture: ComponentFixture<LoadingSpinnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingSpinnerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have isFixPosition set to true by default', () => {
    expect(component.isFixPosition).toBe(true);
  });

  it('should apply position-fixed class when isFixPosition is true', () => {
    const containerElement = fixture.debugElement.query(By.css('.spinner-container'));
    expect(containerElement.nativeElement.classList.contains('position-fixed')).toBe(true);
    expect(containerElement.nativeElement.classList.contains('position-absolute')).toBe(false);
  });

  it('should apply position-absolute class when isFixPosition is false', () => {
    component.isFixPosition = false;
    fixture.detectChanges();
    const containerElement = fixture.debugElement.query(By.css('.spinner-container'));
    expect(containerElement.nativeElement.classList.contains('position-absolute')).toBe(true);
    expect(containerElement.nativeElement.classList.contains('position-fixed')).toBe(false);
  });

  it('should contain a spinner-border element', () => {
    const spinnerElement = fixture.debugElement.query(By.css('.spinner-border'));
    expect(spinnerElement).toBeTruthy();
  });
});
