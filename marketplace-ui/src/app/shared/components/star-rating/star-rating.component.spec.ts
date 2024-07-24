import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StarRatingComponent } from './star-rating.component';
import { By } from '@angular/platform-browser';
import { NgbRatingModule } from '@ng-bootstrap/ng-bootstrap';

describe('StarRatingComponent', () => {
  let component: StarRatingComponent;
  let fixture: ComponentFixture<StarRatingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StarRatingComponent, NgbRatingModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StarRatingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default inputs', () => {
    expect(component.rate).toBe(0);
    expect(component.isReadOnly).toBe(false);
    expect(component.starClass).toBe('');
  });

  it('should display the correct number of stars', () => {
    component.rate = 3;
    fixture.detectChanges();

    const stars = fixture.debugElement.queryAll(By.css('.star-feedback'));
    expect(stars.length).toBe(5); // assuming a 5-star rating system
    const filledStars = fixture.debugElement.queryAll(By.css('.filled'));
    expect(filledStars.length).toBe(3);
  });

  it('should emit rateChange event when rate changes', () => {
    spyOn(component.rateChange, 'emit');

    component.onRateChange(4);

    expect(component.rate).toBe(4);
    expect(component.rateChange.emit).toHaveBeenCalledWith(4);
  });
});
