import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { LoadingSpinnerComponent } from './loading-spinner.component';
import { LoadingComponentId } from '../../enums/loading-component-id';

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

  it('should create', () => expect(component).toBeTruthy());

  it('should display when isLoading state is true', () => {
    component.key = LoadingComponentId.LANDING_PAGE;
    component.loadingService.showLoading(LoadingComponentId.LANDING_PAGE);
    fixture.detectChanges();
    expect(component.isLoading()).toBeTrue();
  });

  it('should display when isLoading state is false', () => {
    component.key = LoadingComponentId.LANDING_PAGE;
    component.loadingService.hideLoading(LoadingComponentId.LANDING_PAGE);
    fixture.detectChanges();
    expect(component.isLoading()).toBeFalse();
  });

  it('container class should come from input', () => {
    component.key = LoadingComponentId.LANDING_PAGE;
    component.containerClasses = 'spinner-container';
    component.loadingService.showLoading(LoadingComponentId.LANDING_PAGE);
    fixture.detectChanges();
    const containerElement = fixture.debugElement.query(
      By.css('.spinner-container')
    );
    expect(containerElement.nativeElement).toBeTruthy();
  });
});
