import { Directive, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';

@Directive({
  selector: '[starRatingHighlight]',
  standalone: true
})
export class StarRatingHighlightDirective implements OnChanges {

  @Input() percent = 0;

  constructor(private readonly el: ElementRef<HTMLElement>) {}

 ngOnChanges(changes: SimpleChanges): void {
    console.log('ngOnChanges', this.percent, changes);

    if (changes['percent']) {
      this.width(this.percent);
    }
  }

  private width(percent: number): void {
    console.log('setting width', percent);
    this.el.nativeElement.style.width = `${percent}%`;
  }
}