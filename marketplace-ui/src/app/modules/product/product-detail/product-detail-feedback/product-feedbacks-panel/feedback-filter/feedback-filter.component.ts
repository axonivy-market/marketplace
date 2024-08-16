import { Component, EventEmitter, inject, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';
import { FormsModule } from '@angular/forms';
import { ProductFeedbackService } from '../product-feedback.service';
import { LanguageService } from '../../../../../../core/services/language/language.service';

@Component({
  selector: 'app-feedback-filter',
  standalone: true,
  imports: [FormsModule, TranslateModule],
  templateUrl: './feedback-filter.component.html',
  styleUrl: './feedback-filter.component.scss'
})
export class FeedbackFilterComponent {
  feedbackSortTypes = FEEDBACK_SORT_TYPES;

  @Output() sortChange = new EventEmitter<string>();

  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);

  onSortChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.sortChange.emit(selectElement.value);
  }
}
