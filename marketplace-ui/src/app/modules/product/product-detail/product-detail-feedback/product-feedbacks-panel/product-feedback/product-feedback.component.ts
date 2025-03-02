import { Component, computed, ElementRef, inject, Input, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { TimeAgoPipe } from '../../../../../../shared/pipes/time-ago.pipe';
import { LanguageService } from '../../../../../../core/services/language/language.service';
import { TranslateModule } from '@ngx-translate/core';
import { FeedbackStatus } from '../../../../../../shared/enums/feedback-status.enum';
import { ProductFeedbackService } from '../product-feedback.service';

@Component({
  selector: 'app-product-feedback',
  standalone: true,
  imports: [CommonModule, StarRatingComponent, TimeAgoPipe, TranslateModule],
  templateUrl: './product-feedback.component.html',
  styleUrl: './product-feedback.component.scss'
})
export class ProductFeedbackComponent {
  @Input() feedback!: Feedback;
  @ViewChild('content') contentElement!: ElementRef;

  productFeedbackService = inject(ProductFeedbackService);
  private resizeObserver!: ResizeObserver;
  private readonly scrollHeight = signal(0);
  private readonly clientHeight = signal(0);

  private readonly isPending = signal(false);
  isFeedbackPending = computed(() => this.isPending());

  showToggle = computed(() => this.scrollHeight() > this.clientHeight() || this.feedback.isExpanded);
  languageService = inject(LanguageService);

  ngOnInit(): void {
    const userFeedback = this.productFeedbackService.userFeedback();
    if (userFeedback && userFeedback.feedbackStatus === FeedbackStatus.PENDING) {
      this.isPending.set(true);
    } else {
      this.isPending.set(false);
    }
  }

  ngAfterViewInit() {
    this.initializeResizeObserver();
  }

  private initializeResizeObserver() {
    this.resizeObserver = new ResizeObserver(() => this.updateHeights());
    this.resizeObserver.observe(this.contentElement.nativeElement);
  }

  private updateHeights() {
    if (this.contentElement) {
      const element = this.contentElement.nativeElement;
      this.scrollHeight.set(element.scrollHeight);
      this.clientHeight.set(element.clientHeight);
    }
  }

  toggleContent() {
    this.feedback.isExpanded = !this.feedback.isExpanded;
  }
}
