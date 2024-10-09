import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';
import { FormsModule } from '@angular/forms';
import { ProductFeedbackService } from '../product-feedback.service';
import { LanguageService } from '../../../../../../core/services/language/language.service';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';
import { CommonUtils } from '../../../../../../shared/utils/common.utils';
import { ItemDropdown } from '../../../../../../shared/models/item-dropdown.model';
import { FeedbackSortType } from '../../../../../../shared/enums/feedback-sort-type';
import { FeedbackFilterService } from './feedback-filter.service';

@Component({
  selector: 'app-feedback-filter',
  standalone: true,
  imports: [FormsModule, TranslateModule, CommonDropdownComponent],
  templateUrl: './feedback-filter.component.html',
  styleUrl: './feedback-filter.component.scss'
})
export class FeedbackFilterComponent implements OnInit {
  feedbackSortTypes = FEEDBACK_SORT_TYPES;

  @Output() sortChange = new EventEmitter<string>();

  feedbackFilterService = inject(FeedbackFilterService);

  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);
  selectedSortTypeLabel: string = CommonUtils.getLabel(FEEDBACK_SORT_TYPES[0].value, FEEDBACK_SORT_TYPES);

  ngOnInit() {
    if (this.feedbackFilterService.data) {
      this.changeSortByLabel(this.feedbackFilterService.data);
    }
    this.feedbackFilterService.event$.subscribe(event => {
      this.changeSortByLabel(event);
    });
  }

  onSortChange(event: ItemDropdown<FeedbackSortType>): void {
    this.changeSortByLabel(event);
    this.sortChange.emit(event.sortFn);
    this.feedbackFilterService.changeSortByLabel(event);
  }

  changeSortByLabel(event: ItemDropdown<FeedbackSortType>): void {
    this.selectedSortTypeLabel = CommonUtils.getLabel(
      event.value,
      FEEDBACK_SORT_TYPES
    );
  }
}
