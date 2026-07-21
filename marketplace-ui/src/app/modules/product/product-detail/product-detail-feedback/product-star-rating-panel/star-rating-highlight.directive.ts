import { Directive, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';

@Directive({
  selector: '[starRatingHighlight]',
  standalone: true
})
export class StarRatingHighlightDirective implements OnChanges {

  @Input() percent = 0;

  constructor(private readonly el: ElementRef<HTMLElement>) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['percent']) {
      this.width(this.percent);
    }
  }

  private width(percent: number): void {
    this.el.nativeElement.style.width = `${percent}%`;
  }
}