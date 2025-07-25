import {
  Component,
  computed,
  ElementRef,
  Inject,
  inject,
  Input,
  PLATFORM_ID,
  signal,
  ViewChild
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { TimeAgoPipe } from '../../../../../../shared/pipes/time-ago.pipe';
import { LanguageService } from '../../../../../../core/services/language/language.service';
import { TranslateModule } from '@ngx-translate/core';
import { FeedbackStatus } from '../../../../../../shared/enums/feedback-status.enum';
import { ProductFeedbackService } from '../product-feedback.service';
import { AuthService } from '../../../../../../auth/auth.service';

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
  authService = inject(AuthService);

  private resizeObserver!: ResizeObserver;
  private readonly scrollHeight = signal(0);
  private readonly clientHeight = signal(0);
  isBrowser: boolean;

  showToggle = computed(
    () => this.scrollHeight() > this.clientHeight() || this.feedback.isExpanded
  );
  languageService = inject(LanguageService);

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  isFeedbackPending(feedback: Feedback): boolean {
    return (
      feedback.userId === this.authService.getUserId() &&
      feedback.feedbackStatus === FeedbackStatus.PENDING
    );
  }

  ngAfterViewInit() {
    if (this.isBrowser) {
      this.initializeResizeObserver();
    }
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
