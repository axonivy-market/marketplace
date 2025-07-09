import {
  Component,
  computed,
  EventEmitter,
  Inject,
  inject,
  Output,
  PLATFORM_ID,
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
import { isPlatformBrowser } from '@angular/common';

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
  isBrowser: boolean;

  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

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
