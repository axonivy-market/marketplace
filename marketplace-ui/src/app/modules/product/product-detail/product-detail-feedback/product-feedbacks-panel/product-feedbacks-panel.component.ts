import {
  Component,
  EventEmitter,
  inject,
  Input,
  input,
  Output,
  Signal
} from '@angular/core';
import { ProductFeedbackComponent } from './product-feedback/product-feedback.component';
import { ProductFeedbackService } from './product-feedback.service';
import { FeedbackFilterComponent } from './feedback-filter/feedback-filter.component';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../../../core/services/theme/theme.service';
import { Feedback } from '../../../../../shared/models/feedback.model';
import { CommonModule } from '@angular/common';
import { ProductDetailService } from '../../product-detail.service';
import { LanguageService } from '../../../../../core/services/language/language.service';

interface CustomElement extends HTMLElement {
  scrollTop: number;
  offsetHeight: number;
  scrollHeight: number;
}

@Component({
  selector: 'app-product-feedbacks-panel',
  standalone: true,
  imports: [
    CommonModule,
    ProductFeedbackComponent,
    FeedbackFilterComponent,
    TranslateModule
  ],
  templateUrl: './product-feedbacks-panel.component.html',
  styleUrl: './product-feedbacks-panel.component.scss'
})
export class ProductFeedbacksPanelComponent {
  isMobileMode = input<boolean>();

  @Input() isRenderInModalDialog = false;
  @Output() showFeedbacksLoadedBtn = new EventEmitter<void>();

  themeService = inject(ThemeService);
  productFeedbackService = inject(ProductFeedbackService);
  productDetailService = inject(ProductDetailService);
  languageService = inject(LanguageService);

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.feedbacks;

  onSortChange(sort: string): void {
    this.productFeedbackService.changeSort(sort);
  }

  onScrollCheckAllFeedbacksLoaded(e: Event): void {
    const element = e.target as CustomElement;
    const threshold = 50;
    const position = element.scrollTop + element.offsetHeight;
    const height = element.scrollHeight;
    if (
      position >= height - threshold &&
      !this.productFeedbackService.areAllFeedbacksLoaded()
    ) {
      this.productFeedbackService.loadMoreFeedbacks();
    }
  }
}
