import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { FILTER_TYPES, SORT_TYPES, DESIGNER_SESSION_STORAGE_VARIABLE } from '../../../shared/constants/common.constant';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import { SortOption } from '../../../shared/enums/sort-option.enum';
import { LanguageService } from '../../../core/services/language/language.service';
import { CommonDropdownComponent } from '../../../shared/components/common-dropdown/common-dropdown.component';
import { CommonUtils } from '../../../shared/utils/common.utils';
import { ItemDropdown } from '../../../shared/models/item-dropdown.model';
import { MatomoAction, MatomoCategory, MatomoTracker } from '../../../shared/enums/matomo-tracking.enum';
import { MATOMO_DIRECTIVES } from 'ngx-matomo-client';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-product-filter',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, CommonDropdownComponent, MATOMO_DIRECTIVES],
  templateUrl: './product-filter.component.html',
  styleUrl: './product-filter.component.scss'
})
export class ProductFilterComponent {
  @Output() searchChange = new EventEmitter<string>();
  @Output() filterChange = new EventEmitter<ItemDropdown<TypeOption>>();
  @Output() sortChange = new EventEmitter<SortOption>();
  protected MatomoCategory = MatomoCategory;
  protected MatomoAction = MatomoAction;
  protected MatomoTracker = MatomoTracker;

  selectedTypeLabel: string = CommonUtils.getLabel(FILTER_TYPES[0].value, FILTER_TYPES);
  selectedSortLabel: string = CommonUtils.getLabel(SORT_TYPES[0].value, SORT_TYPES);
  types = FILTER_TYPES;
  sorts = SORT_TYPES;
  searchText = '';

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  route = inject(ActivatedRoute);
  router = inject(Router);

  constructor() {
    this.route.queryParams.subscribe(params => {
      const type = params['type'] || FILTER_TYPES[0].value;

      const validTypeValues = Object.values(TypeOption);
      const isValidType = validTypeValues.includes(params['type']);
      const queryParams = isValidType
        ? { type: type }
        : { type: null };

      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: queryParams,
        queryParamsHandling: 'merge'
      });

      const selectedType = this.types.find(t => t.value === type);
      if (selectedType) {
        this.filterChange.emit(selectedType);
      }

      this.searchText = params['search'] || '';

      const validSortValues = Object.values(SortOption);
      if (params['sort'] && validSortValues.includes(params['sort'])) {
        this.onSortChange(params['sort']);
      } else {
        this.onSortChange(SortOption.STANDARD);
        const queryParams = validSortValues.includes(params['sort'])
          ? { sort: params['sort'] }
          : { sort: null };

        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: queryParams,
          queryParamsHandling: 'merge'
        });
      }
    });
  }

  onSelectType(type: ItemDropdown<TypeOption>) {
    this.selectedTypeLabel = CommonUtils.getLabel(type.value, this.types);
    this.filterChange.emit(type);
  }

  onSearchChanged(searchString: string) {
    if (searchString) {
      searchString = searchString.trim();
    }
    this.searchChange.next(searchString);
  }

  onSortChange(sort: SortOption) {
    this.sortChange.next(sort);
    this.selectedSortLabel = CommonUtils.getLabel(sort, this.sorts);
  }

}
