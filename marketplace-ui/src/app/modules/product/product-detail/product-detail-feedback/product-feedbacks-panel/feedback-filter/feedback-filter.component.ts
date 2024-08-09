import { Component, EventEmitter, inject, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';
import { FormsModule } from '@angular/forms';
import { ProductFeedbackService } from '../product-feedback.service';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';

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
  selectedSortTypeLabel = FEEDBACK_SORT_TYPES[0].label;
  onSortChange(event: any): void {
    this.selectedSortTypeLabel = this.getLabel(event.label);
    this.sortChange.emit(event.sortFn);
  }

  getLabel(label: string): string {
    const currentLabel = FEEDBACK_SORT_TYPES.find(sortType => sortType.label === label)?.label;
    return currentLabel ? currentLabel : '';
  }
}
