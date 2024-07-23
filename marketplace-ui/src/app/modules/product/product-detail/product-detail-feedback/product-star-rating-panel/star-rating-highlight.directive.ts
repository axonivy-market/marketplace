import { Directive, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[starRatingHighlight]',
  standalone: true
})
export class StarRatingHighlightDirective {

  @Input() percent!: number;

  constructor(private readonly el: ElementRef) { }

  ngOnChanges() {
    this.width(this.percent);
  }

  private width(percent: number) {
    this.el.nativeElement.style.width = percent + "%";
  }
}
