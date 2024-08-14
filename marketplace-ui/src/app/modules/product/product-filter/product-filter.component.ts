import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { FILTER_TYPES, SORT_TYPES } from '../../../shared/constants/common.constant';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import { SortOption } from '../../../shared/enums/sort-option.enum';
import { LanguageService } from '../../../core/services/language/language.service';
import { CommonDropdownComponent } from '../../../shared/components/common-dropdown/common-dropdown.component';

@Component({
  selector: 'app-product-filter',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, CommonDropdownComponent],
  templateUrl: './product-filter.component.html',
  styleUrl: './product-filter.component.scss'
})
export class ProductFilterComponent {
  @Output() searchChange = new EventEmitter<string>();
  @Output() filterChange = new EventEmitter<TypeOption>();
  @Output() sortChange = new EventEmitter<SortOption>();

  selectedTypeLabel = FILTER_TYPES[0].label;
  types = FILTER_TYPES;
  selectedSort: string = SORT_TYPES[0].label;
  sorts = SORT_TYPES;
  searchText = '';

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);

  onSelectType(type: TypeOption) {
    this.selectedTypeLabel = this.getLabel(type , this.types);
    this.filterChange.emit(type);
  }

  onSearchChanged(searchString: string) {
    this.searchChange.next(searchString);
  }

  onSortChange(sort: SortOption) {
    this.sortChange.next(sort);
    this.selectedSort = this.getLabel(sort, this.sorts);
  }

  getLabel(value: string, options: any): string {
    const currentLabel = options.find((option: { value: string, label: string; }) => option.value === value)?.label;
    return currentLabel ? currentLabel : options[0].label;
  }
}
