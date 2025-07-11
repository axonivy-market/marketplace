import {
  Component,
  computed,
  EventEmitter,
  inject,
  Output,
  Signal
} from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';
import { FormsModule } from '@angular/forms';
import { ProductFeedbackService } from '../product-feedback.service';
import { LanguageService } from '../../../../../../core/services/language/language.service';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';
import { CommonUtils } from '../../../../../../shared/utils/common.utils';
import { ItemDropdown } from '../../../../../../shared/models/item-dropdown.model';
import { FeedbackSortType } from '../../../../../../shared/enums/feedback-sort-type';

@Component({
  selector: 'app-feedback-filter',
  standalone: true,
  imports: [FormsModule, TranslateModule, CommonDropdownComponent],
  templateUrl: './feedback-filter.component.html',
  styleUrl: './feedback-filter.component.scss'
})
export class FeedbackFilterComponent {
  feedbackSortTypes = FEEDBACK_SORT_TYPES;

  @Output() sortChange = new EventEmitter<string>();

  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);

  selectedSortTypeLabel: Signal<string> = computed(() =>
    CommonUtils.getLabel(
      this.productFeedbackService.sort(),
      FEEDBACK_SORT_TYPES
    )
  );

  onSortChange(event: ItemDropdown<FeedbackSortType>): void {
    this.productFeedbackService.sort.set(event.value);
    this.sortChange.emit(event.value);
  }
}
