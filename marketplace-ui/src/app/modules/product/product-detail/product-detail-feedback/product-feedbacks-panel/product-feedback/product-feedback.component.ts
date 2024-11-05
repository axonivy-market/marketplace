import { Component, ElementRef, HostListener, inject, Input, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { TimeAgoPipe } from '../../../../../../shared/pipes/time-ago.pipe';
import { LanguageService } from '../../../../../../core/services/language/language.service';

@Component({
  selector: 'app-product-feedback',
  standalone: true,
  imports: [CommonModule, StarRatingComponent, TimeAgoPipe],
  templateUrl: './product-feedback.component.html',
  styleUrl: './product-feedback.component.scss'
})
export class ProductFeedbackComponent {
  @Input() feedback!: Feedback;
  @ViewChild('content') contentElement!: ElementRef;

  showToggle = signal(false);
  isExpanded = signal(false);
  languageService = inject(LanguageService);

  ngAfterViewInit() {
    this.setShowToggle();
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.setShowToggle();
  }

  private setShowToggle() {
    this.showToggle.set(this.contentElement.nativeElement.scrollHeight > this.contentElement.nativeElement.clientHeight);
  }

  toggleContent() {
    this.isExpanded.set(!this.isExpanded());
  }
}
