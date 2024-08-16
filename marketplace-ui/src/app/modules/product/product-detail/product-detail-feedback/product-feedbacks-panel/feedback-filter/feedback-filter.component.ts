import { Component, EventEmitter, inject, Output } from '@angular/core';
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
  selectedSortTypeLabel: string = CommonUtils.getLabel(FEEDBACK_SORT_TYPES[0].value, FEEDBACK_SORT_TYPES);

  onSortChange(event: ItemDropdown<FeedbackSortType>): void {
    this.selectedSortTypeLabel = CommonUtils.getLabel(event.value, FEEDBACK_SORT_TYPES);
    this.sortChange.emit(event.sortFn);
  }

}
