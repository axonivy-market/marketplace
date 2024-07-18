import { StarRatingHighlightDirective } from './star-rating-highlight.directive';
import { Component, ElementRef } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';

@Component({
  template: `<div starRatingHighlight [percent]="percent"></div>`
})
class TestComponent {
  percent = 50;
}

describe('StarRatingHighlightDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let component: TestComponent;
  let el: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StarRatingHighlightDirective],
      declarations: [TestComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    el = fixture.nativeElement.querySelector('div');
  });

  it('should create an instance', () => {
    const directive = new StarRatingHighlightDirective(new ElementRef(el));
    expect(directive).toBeTruthy();
  });

  it('should set the width based on percent input', () => {
    component.percent = 75;
    fixture.detectChanges();
    expect(el.style.width).toBe('75%');

    component.percent = 25;
    fixture.detectChanges();
    expect(el.style.width).toBe('25%');
  });

  it('should update the width when percent input changes', () => {
    component.percent = 50;
    fixture.detectChanges();
    expect(el.style.width).toBe('50%');

    component.percent = 100;
    fixture.detectChanges();
    expect(el.style.width).toBe('100%');
  });
});
