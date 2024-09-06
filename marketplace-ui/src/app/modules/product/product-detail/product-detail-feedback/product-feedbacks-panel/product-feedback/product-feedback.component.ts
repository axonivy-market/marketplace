import { Component, ElementRef, HostListener, Input, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';

@Component({
  selector: 'app-product-feedback',
  standalone: true,
  imports: [CommonModule, StarRatingComponent],
  templateUrl: './product-feedback.component.html',
  styleUrl: './product-feedback.component.scss'
})
export class ProductFeedbackComponent {
  @Input() feedback!: Feedback;
  @ViewChild('content') contentElement!: ElementRef;

  showToggle = signal(false);
  isExpanded = signal(false);

  ngAfterViewInit() {
    this.setShowToggle();
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    console.log(61);
    this.setShowToggle();
  }

  private setShowToggle() {
        console.log(60);

    this.showToggle.set(this.contentElement.nativeElement.scrollHeight > this.contentElement.nativeElement.clientHeight);
  }

  toggleContent() {
        console.log(59);

    this.isExpanded.set(!this.isExpanded());
  }
}
