import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { describe, beforeEach, expect, it } from 'vitest';
import { StarRatingHighlightDirective } from './star-rating-highlight.directive';

@Component({
  standalone: true,
  imports: [StarRatingHighlightDirective],
  template: `<div starRatingHighlight [percent]="percent"></div>`
})
class TestComponent {
  percent = 50;
}

describe('StarRatingHighlightDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let component: TestComponent;
  let el: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    el = fixture.nativeElement.querySelector('div');
  });

  it('should create the directive', () => {
    const directive = fixture.debugElement
      .query(By.directive(StarRatingHighlightDirective))
      .injector.get(StarRatingHighlightDirective);

    expect(directive).toBeTruthy();
  });

  it('should update width when percent changes', async () => {
    expect(el.style.width).toBe('50%');

    component.percent = 75;
    fixture.detectChanges();
    
    const debugEl = fixture.debugElement.query(
    By.directive(StarRatingHighlightDirective)
    );

    const directive = debugEl.injector.get(StarRatingHighlightDirective);

    directive.percent = 75;
    directive.ngOnChanges({
      percent: {
        previousValue: 50,
        currentValue: 75,
        firstChange: false,
        isFirstChange: () => false
      }
    });

    expect(el.style.width).toBe('75%');

    directive.percent = 100;
    directive.ngOnChanges({
      percent: {
        previousValue: 75,
        currentValue: 100,
        firstChange: false,
        isFirstChange: () => false
      }
    });
    expect(el.style.width).toBe('100%');
  });
});